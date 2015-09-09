package org.sonar.plugins.text.batch;

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
import org.sonar.plugins.text.checks.AbstractTextCheck;
import org.sonar.plugins.text.checks.CheckRepository;
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
  public TextIssueSensor(FileSystem fs, ResourcePerspectives perspectives, CheckFactory checkFactory) {
    this.checks = checkFactory.create(CheckRepository.REPOSITORY_KEY).addAnnotatedChecks(CheckRepository.getCheckClasses());
    this.fs = fs;
    this.resourcePerspectives = perspectives;
  }

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    // This sensor is executed only when there are Text files
    return fs.hasFiles(fs.predicates().hasLanguage("text"));
  }

  @Override
  public void analyse(Project project, SensorContext sensorContext) {
    for (InputFile inputFile : fs.inputFiles(fs.predicates().hasLanguage("text"))) {
        try {
          TextSourceFile textSourceFile = new TextSourceFile(inputFile);
        	
          for (Object check : checks.all()) {
            ((AbstractTextCheck) check).setRuleKey(checks.ruleKey(check));
            ((AbstractTextCheck) check).validate(textSourceFile, project.getKey());
          }
          saveIssue(textSourceFile);

        } catch (Exception e) {
          LOG.error("Could not analyze the file " + inputFile.file().getAbsolutePath(), e);
        }
      }

  }
  
  @VisibleForTesting
  protected void saveIssue(TextSourceFile source) {
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
