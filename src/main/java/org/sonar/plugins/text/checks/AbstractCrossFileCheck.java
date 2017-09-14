package org.sonar.plugins.text.checks;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.sonar.api.batch.fs.InputFile;

public abstract class AbstractCrossFileCheck extends AbstractTextCheck {
  Map<InputFile, List<CrossFileScanPrelimIssue>> crossFileChecksRawResults;

  @Override
  public void validate(final TextSourceFile sourceFile, final String projectKey) {
    throw new UnsupportedOperationException("This is a cross-file check so you must use the form of the 'validate' method that provides a data structure to store the preliminary results.");
  }

  // Later will be moved to a new parent abstract class used by cross-file checks
  protected final void recordMatch(final RulePart rulePart, final Integer linePosition, final String message) {

    CrossFileScanPrelimIssue issueToRecord = new CrossFileScanPrelimIssue(rulePart, this.getRuleKey(), linePosition, message);

    TextSourceFile fileToReportAgainst = getTextSourceFile();
    List<CrossFileScanPrelimIssue> prelimIssuesForOneFile = crossFileChecksRawResults.get(fileToReportAgainst.getInputFile());

    if (prelimIssuesForOneFile == null) {
      prelimIssuesForOneFile = new LinkedList<CrossFileScanPrelimIssue>();
      prelimIssuesForOneFile.add(issueToRecord);
      crossFileChecksRawResults.put(fileToReportAgainst.getInputFile(), prelimIssuesForOneFile);
    } else {
      prelimIssuesForOneFile.add(issueToRecord);
    }


  }

  protected void setCrossFileChecksRawResults(final Map<InputFile, List<CrossFileScanPrelimIssue>> crossFileChecksRawResults) {
    this.crossFileChecksRawResults = crossFileChecksRawResults;
  }

  public abstract void validate(Map<InputFile, List<CrossFileScanPrelimIssue>> crossFileChecksRawResults,
                       TextSourceFile textSourceFile,
                       String projectKey
                      );

  private boolean isRuleTriggerPresent() {
    boolean ruleTriggered = false;
    for (Entry<InputFile, List<CrossFileScanPrelimIssue>> currentInputFileEntry : crossFileChecksRawResults.entrySet()) {
      List<CrossFileScanPrelimIssue> prelimIssues = currentInputFileEntry.getValue();
      for (CrossFileScanPrelimIssue currentPrelimIssue : prelimIssues) {
        if (RulePart.TriggerPattern == currentPrelimIssue.getRulePart() && this.getRuleKey().equals(currentPrelimIssue.getRuleKey())) {
          ruleTriggered = true;
//          System.out.println("Trigger detected during final cross-file processing: " + currentPrelimIssue);
          break;
        }
      }
    }

    return ruleTriggered;
  }

  /**
   * After the first "labelling" pass analyze the files that were flagged to determine which need an issue raised for the current check
   * @param
   * @return
   */
  public List<TextSourceFile> raiseIssuesAfterScan() {
    List<TextSourceFile> textSourceFiles = new LinkedList<TextSourceFile>();

    if (isRuleTriggerPresent()) {
      raiseAppropriateViolationsAgainstSourceFiles(textSourceFiles);
    }
    return textSourceFiles;
  }

  abstract protected void raiseAppropriateViolationsAgainstSourceFiles(List<TextSourceFile> sourceFiles);

  enum RulePart {
    TriggerPattern("TriggerPattern"), DisallowPattern("DisallowPattern"), MustAlsoExistPattern("MustAlsoExistPattern");
    String value;

    RulePart(final String value) {
      this.value = value;
    }
  }

}
