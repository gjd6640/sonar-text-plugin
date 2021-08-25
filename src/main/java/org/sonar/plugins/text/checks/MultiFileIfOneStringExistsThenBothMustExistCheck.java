package org.sonar.plugins.text.checks;

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

@Rule(key = "MultiFileIfOneStringExistsThenBothMustExistCheck",
      priority = Priority.MAJOR,
      name = "If a string is present then another string must also be present (cross-file)", description = "Checks for a 'trigger match' in one file. Only if that is present a second expression is checked against a defined set of files. If that other expression is not present in the project the triggering line of code will have an issue raised against it. Regex is applied in simple non-DOTALL mode / is single-line-based.")
public class MultiFileIfOneStringExistsThenBothMustExistCheck extends AbstractCrossFileCheck {
  private static final Logger LOG = LoggerFactory.getLogger(MultiFileIfOneStringExistsThenBothMustExistCheck.class);
  protected static final int MAX_CHARACTERS_SCANNED = 500001;

  @RuleProperty(key = "triggerExpression", type = "TEXT", defaultValue = "^some single-line.*regex search string$")
  private String triggerExpression;

  @RuleProperty(key = "triggerFilePattern", defaultValue = "**/*.properties", description = "Ant Style path expression. To include all of the files in this project use '**/*'. \n\nFiles scanned will be limited by the list of file extensions configured for this language AND by the values of 'sonar.sources' and 'sonar.exclusions'. Also, using just 'filename.txt' here to point the rule to a file at the root of the project does not appear to work (as of SQ v4.5.5). Use '**/filename.txt' instead.")
  private String triggerFilePattern;

  @RuleProperty(key = "mustAlsoExistExpression", type = "TEXT", defaultValue = "^some single-line.*regex search string$")
  private String mustAlsoExistExpression;

  @RuleProperty(key = "mustAlsoExistFilePattern", defaultValue = "**/*.properties", description = "Ant Style path expression. To include all of the files in this project use '**/*'. \n\nFiles scanned will be limited by the list of file extensions configured for this language AND by the values of 'sonar.sources' and 'sonar.exclusions'. Also, using just 'filename.txt' here to point the rule to a file at the root of the project does not appear to work (as of SQ v4.5.5). Use '**/filename.txt' instead.")
  private String mustAlsoExistFilePattern;

  @RuleProperty(type = "BOOLEAN", key = "applyExpressionToOneLineOfTextAtATime", defaultValue = "true", description = "Select this to feed the regular expression evaluator one line at a time. Uncheck it if your expression needs to 'see' multiple lines. When not checked only the first " + (MAX_CHARACTERS_SCANNED-1) + " characters of each file will be processed.")
  private boolean applyExpressionToOneLineOfTextAtATime = true;

  @RuleProperty(key = "message")
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

    if (shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())) {
      recordMatches(textSourceFile, RulePart.TriggerPattern);
      recordMatches(textSourceFile, RulePart.MustAlsoExistPattern);
    }
  }

  private void recordMatches(final TextSourceFile textSourceFile, final RulePart rulePart) {
    final String regularExpression;
    final String fileMatchPattern;

    if (RulePart.TriggerPattern.equals(rulePart)) {
      regularExpression = triggerExpression;
      fileMatchPattern = triggerFilePattern;
    } else {
      regularExpression = mustAlsoExistExpression;
      fileMatchPattern = mustAlsoExistFilePattern;
    }

    if (regularExpression != null && isFileIncluded(fileMatchPattern)) {
      LOG.debug("Checking file: {}", textSourceFile.getInputFile().absolutePath());

      if (applyExpressionToOneLineOfTextAtATime) {
        recordMatchesOneLineAtATime(textSourceFile, regularExpression, rulePart);
      } else {
        recordMatchesUsingDOTALLFriendlyApproach(textSourceFile, regularExpression, rulePart);
      }
    } else {
      LOG.debug("Did not check file '{}' for " + rulePart + " because it looked like a file that I shouldn't process.", textSourceFile.getInputFile().absolutePath());
    }

  }

  private void recordMatchesOneLineAtATime(final TextSourceFile textSourceFile, final String regularExpression, final RulePart recordMatchAsRulePart) {
    Pattern regexp = Pattern.compile(regularExpression);
    Matcher matcher = regexp.matcher(""); // Apply the pattern to search this empty string just to get a matcher reference. We'll reset it in a moment to work against a real string.

    Path path = textSourceFile.getInputFile().file().toPath();
    try (
        LineNumberReader lineReader = new LineNumberReader(Files.newBufferedReader(path, StandardCharsets.UTF_8));
        ) {
          String line = null;
          while ((line = lineReader.readLine()) != null) {
            matcher.reset(line); //reset the input
            if (matcher.find()) {
              LOG.debug("{} match found: '{}' on line {} of file '{}'.", recordMatchAsRulePart.toString(), line, lineReader.getLineNumber(), textSourceFile.getInputFile().file().toPath());
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
      LOG.debug("{} match found: on line {} of file '{}'.", recordMatchAsRulePart.toString(), message, lineNumberOfTriggerMatch, textSourceFile.getInputFile().file().toPath());
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

  public String getMustAlsoExistExpression() {
    return mustAlsoExistExpression;
  }

  public void setMustAlsoExistExpression(final String disallowExpression) {
    this.mustAlsoExistExpression = disallowExpression;
  }

  public String getMustAlsoExistFilePattern() {
    return mustAlsoExistFilePattern;
  }

  public void setMustAlsoExistFilePattern(final String disallowFilePattern) {
    this.mustAlsoExistFilePattern = disallowFilePattern;
  }

  protected Boolean getApplyExpressionToOneLineOfTextAtATime() {
    return applyExpressionToOneLineOfTextAtATime;
  }

  protected void setApplyExpressionToOneLineOfTextAtATime(final Boolean applyExpressionToOneLineOfTextAtATime) {
    this.applyExpressionToOneLineOfTextAtATime = applyExpressionToOneLineOfTextAtATime;
  }

  @Override
  protected void raiseAppropriateViolationsAgainstSourceFiles(final List<TextSourceFile> sourceFiles) {
    boolean occurrenceFound = false;

    for (Entry<InputFile, List<CrossFileScanPrelimIssue>> currentInputFileEntry : crossFileChecksRawResults.entrySet()) {
      List<CrossFileScanPrelimIssue> prelimIssues = currentInputFileEntry.getValue();
      setTextSourceFile(new TextSourceFile(currentInputFileEntry.getKey()));

      for (CrossFileScanPrelimIssue currentPrelimIssue : prelimIssues) {
        if (RulePart.MustAlsoExistPattern == currentPrelimIssue.getRulePart()
            && this.getRuleKey().equals(currentPrelimIssue.getRuleKey())) {
          // System.out.println("Raising issue on: " + currentPrelimIssue);
          occurrenceFound = true;
          break;
        }
      }

      if (!occurrenceFound) {
        for (CrossFileScanPrelimIssue currentPrelimIssue : prelimIssues) {
          if (RulePart.TriggerPattern == currentPrelimIssue.getRulePart()
              && this.getRuleKey().equals(currentPrelimIssue.getRuleKey())) {
            createViolation(currentPrelimIssue.getLine(), currentPrelimIssue.getMessage());
          }
        }
        sourceFiles.add(getTextSourceFile());
      }

    }

  }


}
