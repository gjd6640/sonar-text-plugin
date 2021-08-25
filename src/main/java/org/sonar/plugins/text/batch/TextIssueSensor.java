package org.sonar.plugins.text.batch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.plugins.text.TextRulesDefinition;
import org.sonar.plugins.text.checks.AbstractCrossFileCheck;
import org.sonar.plugins.text.checks.AbstractTextCheck;
import org.sonar.plugins.text.checks.TextChecksList;
import org.sonar.plugins.text.checks.CrossFileScanPrelimIssue;
import org.sonar.plugins.text.checks.TextIssue;
import org.sonar.plugins.text.checks.TextSourceFile;

public class TextIssueSensor implements Sensor {
  private final Logger LOG = LoggerFactory.getLogger(TextIssueSensor.class);

  private final Checks<Object> checks;
  private final FileSystem fs;
  private final SensorContext sensorContext;
  private final InputProject project;
  final Map<InputFile, List<CrossFileScanPrelimIssue>> crossFileChecksRawResults;

  /**
   * Use of IoC to get FileSystem
   */
  public TextIssueSensor(final FileSystem fs, SensorContext sensorContext, final CheckFactory checkFactory) {
    this.checks = checkFactory.create(TextChecksList.REPOSITORY_KEY).addAnnotatedChecks((Iterable<?>) TextChecksList.getCheckClasses());

    this.fs = fs;
    this.project = sensorContext.project();
    this.sensorContext = sensorContext;

    // This data structure is shared across all cross-file checks so they can see each others' data.
    // Each file with any trigger or disallow match gets a listitem indicating the specifics of the Check that matched including line number. This object reference stays with the check and gets referenced later inside the "raiseIssuesAfterScan()" method call.
    this.crossFileChecksRawResults = new HashMap<>();
  }

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("Inspects the project's files using custom rules built from regular expressions for known combinations of problematic configurations and/or code.");
    descriptor.createIssuesForRuleRepositories(TextRulesDefinition.REPOSITORY_KEY);
  }

  @Override
  public void execute(final SensorContext sensorContext) {

    for (InputFile inputFile : fs.inputFiles(fs.predicates().hasType(InputFile.Type.MAIN))) {
      analyseIndividualFile(inputFile);
    }

    raiseCrossFileCheckIssues();

  }

  private void analyseIndividualFile(final InputFile inputFile) {
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
        LOG.warn("Check failure details:", e);
      }
    }

    saveIssues(textSourceFile.getTextIssues(), textSourceFile.getInputFile());
  }

  private void raiseCrossFileCheckIssues() {
    for (Object check : checks.all()) {
      if (check instanceof AbstractCrossFileCheck) {
        List<TextSourceFile> textSourceFiles = ((AbstractCrossFileCheck) check).raiseIssuesAfterScan();

        for (TextSourceFile file : textSourceFiles) {
          saveIssues(file.getTextIssues(), file.getInputFile());
        }
      }
    }
  }

  private void saveIssues(final List<TextIssue> issuesList, final InputFile againstThisFile) {
    for (TextIssue issue : issuesList) {
      NewIssue newIssue = sensorContext.newIssue();

      NewIssueLocation primaryLocation = newIssue.newLocation()
          .message(issue.getMessage())
          .on(againstThisFile)
// TODO: Right now I'm not taking on implementing logic to identify the specific characters that matched. Highlighting the entire line instead.
//          .at(againstThisFile.newRange(issue.getLine(), 1, issue.getLine(), 2));
          .at(againstThisFile.selectLine(issue.getLine()));

      newIssue
          .forRule(issue.getRuleKey())
          .at(primaryLocation)
          .save();
    }
  }

}
