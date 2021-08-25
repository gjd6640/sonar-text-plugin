package org.sonar.plugins.text.checks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.text.checks.util.FileIOUtil;
import org.sonar.plugins.text.checks.util.LargeFileEncounteredException;
import org.sonar.plugins.text.checks.util.LineNumberFinderUtil;

@Rule(key = "StringDisallowedIfMatchInAnotherFileCheck",
      priority = Priority.MAJOR,
      name = "String disallowed if a match was found in another file", description = "Checks for a 'trigger match' in one file. Only if that is present a second expression is checked against all files in the project and all matches cause an issue to be raised. Regex is applied in simple non-DOTALL mode / is single-line-based.")
public class StringDisallowedIfMatchInAnotherFileCheck extends AbstractCrossFileCheck {
  private static final Logger LOG = LoggerFactory.getLogger(StringDisallowedIfMatchInAnotherFileCheck.class);
  protected static final int MAX_CHARACTERS_SCANNED = 500001;

  @RuleProperty(key = "triggerExpression", type = "TEXT", defaultValue = "^some single-line.*regex search string$")
  private String triggerExpression;

  @RuleProperty(key = "triggerFilePattern", defaultValue = "**/*.properties", description = "Ant Style path expression. To include all of the files in this project use '**/*'. \n\nFiles scanned will be limited by the list of file extensions configured for this language AND by the values of 'sonar.sources' and 'sonar.exclusions'. Also, using just 'filename.txt' here to point the rule to a file at the root of the project does not appear to work (as of SQ v4.5.5). Use '**/filename.txt' instead.")
  private String triggerFilePattern;

  @RuleProperty(key = "disallowExpression", type = "TEXT", defaultValue = "^some single-line.*regex search string$")
  private String disallowExpression;

  @RuleProperty(key = "disallowFilePattern", defaultValue = "**/*.properties", description = "Ant Style path expression. To include all of the files in this project use '**/*'. \n\nFiles scanned will be limited by the list of file extensions configured for this language AND by the values of 'sonar.sources' and 'sonar.exclusions'. Also, using just 'filename.txt' here to point the rule to a file at the root of the project does not appear to work (as of SQ v4.5.5). Use '**/filename.txt' instead.")
  private String disallowFilePattern;

  @RuleProperty(type = "BOOLEAN", key = "applyExpressionToOneLineOfTextAtATime", defaultValue = "true", description = "Select this to feed the regular expression evaluator one line at a time. Uncheck it if your expression needs to 'see' multiple lines. When not checked only the first " + (MAX_CHARACTERS_SCANNED-1) + " characters of each file will be processed. Since: v0.8. Rules created from this template under pre-v0.8 plugin versions won't show this option and will default to 'true'.")
  private boolean applyExpressionToOneLineOfTextAtATime = true;

  @RuleProperty(
    key = "message")
  private String message;

  @Override
  public void validate(final Map<InputFile, List<CrossFileScanPrelimIssue>> crossFileChecksRawResults,
                        final TextSourceFile textSourceFile,
                        final String projectKey
                      ) {
    setTextSourceFile(textSourceFile);
    setCrossFileChecksRawResults(crossFileChecksRawResults);
    LOG.debug("Current file: {}", textSourceFile.getInputFile().absolutePath());
    LOG.debug("validating");

    if (triggerExpression != null &&
        isFileIncluded(triggerFilePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {
      LOG.debug("Checking file: {}", textSourceFile.getInputFile().absolutePath());

      if (applyExpressionToOneLineOfTextAtATime) {
        recordMatchesOneLineAtATime(textSourceFile, triggerExpression, RulePart.TriggerPattern);
      } else {
        recordMatchesUsingDOTALLFriendlyApproach(textSourceFile, triggerExpression, RulePart.TriggerPattern);
      }
    } else {
      LOG.debug("Did not check file '{}' for trigger because it looked like a file that I shouldn't process.", textSourceFile.getInputFile().absolutePath());
    }

    if (disallowExpression != null &&
        isFileIncluded(disallowFilePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {

      if (applyExpressionToOneLineOfTextAtATime) {
        recordMatchesOneLineAtATime(textSourceFile, disallowExpression, RulePart.DisallowPattern);
      } else {
        recordMatchesUsingDOTALLFriendlyApproach(textSourceFile, disallowExpression, RulePart.DisallowPattern);
      }
    }
  }

  private void recordMatchesOneLineAtATime(final TextSourceFile textSourceFile, final String regularExpression, final RulePart recordMatchAsRulePart) {
    Pattern regexp = Pattern.compile(regularExpression);
    Matcher matcher = regexp.matcher(""); // Apply the pattern to search this empty string just to get a matcher reference. We'll reset it in a moment to work against a real string.

    Path path = textSourceFile.getInputFile().file().toPath();
    try (
        BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        LineNumberReader lineReader = new LineNumberReader(reader);
        ) {
      String line = null;
          while ((line = lineReader.readLine()) != null) {
            matcher.reset(line); //reset the input
            if (matcher.find()) {
              LOG.debug("{} match found: '{}' on line {} of file '{}'.", new Object[]{recordMatchAsRulePart.toString(), line, lineReader.getLineNumber(), textSourceFile.getInputFile().file().toPath()});
              recordMatch(recordMatchAsRulePart, lineReader.getLineNumber(), message);
          }
        }
      }
      catch (IOException ex){
        throw new RuntimeException(ex);
      }

  }

  private void recordMatchesUsingDOTALLFriendlyApproach(final TextSourceFile textSourceFile, final String regularExpression, final RulePart recordMatchAsRulePart) {
    Path path = textSourceFile.getInputFile().file().toPath();
    String entireFileAsString;
    int lineNumberOfTriggerMatch = -1;

    try {
      entireFileAsString = FileIOUtil.readFileAsString(path, MAX_CHARACTERS_SCANNED);
    } catch (LargeFileEncounteredException ex) {
      // Note: The FileIOUtil method logs a warning so this doesn't completely hide/swallow the concern
      return;
    }

    Pattern regexp = Pattern.compile(regularExpression);
    Matcher matcher = regexp.matcher(entireFileAsString);
    if (matcher.find()) {
      int positionOfMatch = matcher.start();
      lineNumberOfTriggerMatch = LineNumberFinderUtil.countLines(entireFileAsString, positionOfMatch);
      LOG.debug("{} match found: on line {} of file '{}'.", new Object[]{recordMatchAsRulePart.toString(), message, lineNumberOfTriggerMatch, textSourceFile.getInputFile().file().toPath()});
      recordMatch(recordMatchAsRulePart, lineNumberOfTriggerMatch, message);
    }

  }

  public String getTriggerExpression() {
    return triggerExpression;
  }

  public String getTriggerFilePattern() {
    return triggerFilePattern;
  }

  public String getMessage() {
    return message;
  }

  public void setTriggerExpression(final String expression) {
    this.triggerExpression = expression;
  }

  public void setTriggerFilePattern(final String filePattern) {
    this.triggerFilePattern = filePattern;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public String getDisallowExpression() {
    return disallowExpression;
  }

  public void setDisallowExpression(final String disallowExpression) {
    this.disallowExpression = disallowExpression;
  }

  public String getDisallowFilePattern() {
    return disallowFilePattern;
  }

  public void setDisallowFilePattern(final String disallowFilePattern) {
    this.disallowFilePattern = disallowFilePattern;
  }

  protected Boolean getApplyExpressionToOneLineOfTextAtATime() {
    return applyExpressionToOneLineOfTextAtATime;
  }

  protected void setApplyExpressionToOneLineOfTextAtATime(final Boolean applyExpressionToOneLineOfTextAtATime) {
    this.applyExpressionToOneLineOfTextAtATime = applyExpressionToOneLineOfTextAtATime;
  }

  @Override
  protected void raiseAppropriateViolationsAgainstSourceFiles(final List<TextSourceFile> sourceFiles) {
    for (Entry<InputFile, List<CrossFileScanPrelimIssue>> currentInputFileEntry : crossFileChecksRawResults.entrySet()) {
      List<CrossFileScanPrelimIssue> prelimIssues = currentInputFileEntry.getValue();
      setTextSourceFile(new TextSourceFile(currentInputFileEntry.getKey()));

      for (CrossFileScanPrelimIssue currentPrelimIssue : prelimIssues) {
        if (RulePart.DisallowPattern == currentPrelimIssue.getRulePart()
              && this.getRuleKey().equals(currentPrelimIssue.getRuleKey())) {
          // System.out.println("Raising issue on: " + currentPrelimIssue);
          createViolation(currentPrelimIssue.getLine(), currentPrelimIssue.getMessage());
        }
      }

      sourceFiles.add(getTextSourceFile());
    }

  }

}
