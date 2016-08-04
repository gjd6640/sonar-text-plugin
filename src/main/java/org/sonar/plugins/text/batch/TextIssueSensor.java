package org.sonar.plugins.text.batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.resources.Project;
import org.sonar.plugins.text.checks.AbstractCrossFileCheck;
import org.sonar.plugins.text.checks.AbstractTextCheck;
import org.sonar.plugins.text.checks.CheckRepository;
import org.sonar.plugins.text.checks.CrossFileScanPrelimIssue;
import org.sonar.plugins.text.checks.TextIssue;
import org.sonar.plugins.text.checks.TextSourceFile;

import com.google.common.annotations.VisibleForTesting;

public class TextIssueSensor implements Sensor {
  private static final Logger LOG = LoggerFactory.getLogger(TextIssueSensor.class);

  private final Checks<Object> checks;
  private final FileSystem fs;
  private final ResourcePerspectives resourcePerspectives;

  /**
   * Use of IoC to get FileSystem
   */
  public TextIssueSensor(final FileSystem fs, final ResourcePerspectives perspectives, final CheckFactory checkFactory) {
    this.checks = checkFactory.create(CheckRepository.REPOSITORY_KEY).addAnnotatedChecks(CheckRepository.getCheckClasses());
    this.fs = fs;
    this.resourcePerspectives = perspectives;
  }

  @Override
  public boolean shouldExecuteOnProject(final Project project) {
    // This sensor is executed only when there are Text files
    return fs.hasFiles(fs.predicates().hasLanguage("text"));
  }

  @Override
  public void analyse(final Project project, final SensorContext sensorContext) {

    // Note that currently this data structure is shared across all cross-file checks so they can see each others' data.
    // Each file with any trigger or disallow match gets a listitem indicating the specifics of the Check that matched including line number. This object reference stays with the check and gets referenced later inside the "raiseIssuesAfterScan()" method call.
    Map<InputFile, List<CrossFileScanPrelimIssue>> crossFileChecksRawResults = new HashMap<InputFile, List<CrossFileScanPrelimIssue>>();


    for (InputFile inputFile : fs.inputFiles(fs.predicates().hasType(InputFile.Type.MAIN))) {
        try {
          TextSourceFile textSourceFile = new TextSourceFile(inputFile);

          for (Object check : checks.all()) {
            if (check instanceof AbstractCrossFileCheck) {
              // Calls to cross-file checks need to pass in the data structure used to collect match data
              ((AbstractCrossFileCheck) check).setRuleKey(checks.ruleKey(check));
              ((AbstractCrossFileCheck) check).validate(crossFileChecksRawResults, textSourceFile, project.getKey());
            } else {
              ((AbstractTextCheck) check).setRuleKey(checks.ruleKey(check));
              ((AbstractTextCheck) check).validate(textSourceFile, project.getKey());
            }
          }
          saveIssue(textSourceFile);

        } catch (Exception e) {
          LOG.error("Could not analyze the file " + inputFile.file().getAbsolutePath(), e);
        }
      }

    for (Object check : checks.all()) {
      if (check instanceof AbstractCrossFileCheck) {
        List<TextSourceFile> textSourceFiles = ((AbstractCrossFileCheck) check).raiseIssuesAfterScan();

        for (TextSourceFile file : textSourceFiles) {
          saveIssue(file);
        }
      }
    }

  }

  @VisibleForTesting
  protected void saveIssue(final TextSourceFile source) {
    for (TextIssue issue : source.getTextIssues()) {
      Issuable issuable = resourcePerspectives.as(Issuable.class, source.getInputFile());

      if (issuable != null) {
        issuable.addIssue(
          issuable.newIssueBuilder()
            .ruleKey(issue.getRuleKey())
            .line(issue.getLine())
            .message(issue.getMessage())
            .build());
      }
    }
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

}
