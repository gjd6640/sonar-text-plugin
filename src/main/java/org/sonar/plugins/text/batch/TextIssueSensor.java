package org.sonar.plugins.text.batch;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.batch.sensor.issue.internal.DefaultIssueLocation;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.fs.TextPointer;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.fs.internal.DefaultTextPointer;
import org.sonar.api.batch.fs.internal.DefaultTextRange;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rules.Rule;
import org.sonar.plugins.text.checks.AbstractCrossFileCheck;
import org.sonar.plugins.text.checks.AbstractTextCheck;
import org.sonar.plugins.text.checks.CheckRepository;
import org.sonar.plugins.text.checks.CrossFileScanPrelimIssue;
import org.sonar.plugins.text.checks.TextIssue;
import org.sonar.plugins.text.checks.TextSourceFile;

import com.google.common.collect.Maps;

public class TextIssueSensor implements Sensor {
  private final Logger LOG = LoggerFactory.getLogger(TextIssueSensor.class);

  private final Checks<Object> checks;
//  private final FileSystem fs;
//  private final ResourcePerspectives resourcePerspectives;
  private final InputModule project;
  final Map<InputFile, List<CrossFileScanPrelimIssue>> crossFileChecksRawResults;

  /**
   * Use of IoC to get FileSystem
   */
  public TextIssueSensor(final CheckFactory checkFactory, final InputModule project) {
    this.checks = checkFactory.create(CheckRepository.REPOSITORY_KEY).addAnnotatedChecks(CheckRepository.getCheckClasses());
//    this.fs = fs;
//    this.resourcePerspectives = perspectives;
    this.project = project;

    // This data structure is shared across all cross-file checks so they can see each others' data.
    // Each file with any trigger or disallow match gets a listitem indicating the specifics of the Check that matched including line number. This object reference stays with the check and gets referenced later inside the "raiseIssuesAfterScan()" method call.
    this.crossFileChecksRawResults = Maps.newHashMap();
  }

  /**
   * This sensor is executed only when there are "text" language files present in the project.
   *
   * Consider in future versions: Will all users want this behavior? This plugin now scans files from other languages when the rule's ant-style file path pattern directs it to...
   */
/*
  private boolean shouldExecuteOnProject(final InputModule project) {
    return fs.hasFiles(fs.predicates().hasLanguage("text"));
  }
*/
  @Override
  public void execute(final SensorContext sensorContext) {
    FileSystem fs = sensorContext.fileSystem();
    for (InputFile inputFile : fs.inputFiles(fs.predicates().hasType(InputFile.Type.MAIN))) {
      analyseIndividualFile(inputFile, sensorContext);
    }

    raiseCrossFileCheckIssues(sensorContext);
  }

  private void analyseIndividualFile(final InputFile inputFile, SensorContext sensorContext) {
    TextSourceFile textSourceFile = new TextSourceFile(inputFile);

    for (Object check : checks.all()) {
      try {
        if (check instanceof AbstractCrossFileCheck) {
          // Calls to cross-file checks need to pass in the data structure used to collect match data
          AbstractCrossFileCheck crossFileCheck = (AbstractCrossFileCheck) check;
          crossFileCheck.setRuleKey(checks.ruleKey(check));
          crossFileCheck.validate(crossFileChecksRawResults, textSourceFile, project.key());
        } else {
          AbstractTextCheck textCheck = (AbstractTextCheck) check;
          textCheck.setRuleKey(checks.ruleKey(check));
          textCheck.validate(textSourceFile, project.key());
        }
      } catch (Exception e) {
        LOG.warn("Check for rule \"{}\" choked on file {}. Continuing the scan. Skipping evaluation of just this one rule against this one file.", ((AbstractTextCheck) check).getRuleKey(), inputFile.file().getAbsolutePath());
        LOG.warn("Brief failure cause info: " + e.toString());
        LOG.warn("Full failure details can be exposed by enabling debug logging on 'org.sonar.plugins.text.batch.TextIssueSensor'.");
        LOG.debug("Check failure details:", e);
      }
    }

    saveIssues(textSourceFile.getTextIssues(), textSourceFile.getInputFile(), sensorContext);
  }

  private void raiseCrossFileCheckIssues(SensorContext sensorContext) {
    for (Object check : checks.all()) {
      if (check instanceof AbstractCrossFileCheck) {
        List<TextSourceFile> textSourceFiles = ((AbstractCrossFileCheck) check).raiseIssuesAfterScan();

        for (TextSourceFile file : textSourceFiles) {
          saveIssues(file.getTextIssues(), file.getInputFile(), sensorContext);
        }
      }
    }
  }

  private void saveIssues(final List<TextIssue> issuesList, final InputFile againstThisFile, SensorContext sensorContext) {
    for (TextIssue issue : issuesList) {
      sensorContext.newIssue()
          .at(new DefaultIssueLocation().on(againstThisFile).at(againstThisFile.selectLine(issue.getLine())))
          .forRule(issue.getRuleKey())
          .save();
    }
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("TextIssueSensor");
  }

}
