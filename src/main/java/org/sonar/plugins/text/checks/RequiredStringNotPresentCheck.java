package org.sonar.plugins.text.checks;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.text.checks.util.FileIOUtil;
import org.sonar.plugins.text.checks.util.LargeFileEncounteredException;
import org.sonar.plugins.text.checks.util.LineNumberFinderUtil;
import org.sonar.squidbridge.annotations.RuleTemplate;

@Rule(key = "RequiredStringNotPresentRegexMatchCheck",
      priority = Priority.MAJOR,
      name = "Required String not Present", description = "Allows you to enforce \"When string 'A' is present string 'B' must also be present\". Raises an issue when text in the file matches to some 'trigger' regular expression but none match to a 'must exist' regular expression. The regular expression evaluation uses Java's Pattern.DOTALL option so '.*' will match past newline characters. Note that ^ and $ character matching is to beginning and end of file UNLESS you start your expression with (?m).")
@RuleTemplate
public class RequiredStringNotPresentCheck extends AbstractTextCheck {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTextCheck.class);

  @RuleProperty(key = "triggerRegularExpression", type = "TEXT", defaultValue = "(?m)^some.*regex search string$")
  private String triggerExpression;

  @RuleProperty(key = "mustExistRegularExpression", type = "TEXT", defaultValue = "(?m)^some.*regex search string$")
  private String mustExistExpression;

  @RuleProperty(key = "filePattern", defaultValue = "**/*.properties", description = "Ant Style path expression. To include all of the files in this project use '**/*'. \n\nFiles scanned will be limited by the list of file extensions configured for this language AND by the values of 'sonar.sources' and 'sonar.exclusions'. Also, using just 'filename.txt' here to point the rule to a file at the root of the project does not appear to work (as of SQ v4.5.5). Use '**/filename.txt' instead.")
  private String filePattern;

  @RuleProperty(
    key = "message")
  private String message;

  public String getExpression() {
    return triggerExpression;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public String getMessage() {
    return message;
  }

  public void setTriggerExpression(final String expression) {
    this.triggerExpression = expression;
  }

  public void setMustExistExpression(final String mustExistExpression) {
    this.mustExistExpression = mustExistExpression;
  }

  public void setFilePattern(final String filePattern) {
    this.filePattern = filePattern;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  protected static final int MAX_CHARACTERS_SCANNED = 500001;

  @Override
  public void validate(final TextSourceFile textSourceFile, final String projectKey) {
    boolean triggerMatchFound = false;
    int lineNumberOfTriggerMatch = -1;
    boolean mustExistMatchFound = false;

    setTextSourceFile(textSourceFile);

    if (triggerExpression != null && mustExistExpression != null &&
        isFileIncluded(filePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {

      Path path = textSourceFile.getInputFile().file().toPath();
      String entireFileAsString;
      try {
        entireFileAsString = FileIOUtil.readFileAsString(path, MAX_CHARACTERS_SCANNED);
      } catch (LargeFileEncounteredException ex) {
//        System.out.println("Skipping file. Text scanner (" + this.getClass().getSimpleName() + ") maximum file size ( " + (MAX_CHARACTERS_SCANNED-1) + " chars) encountered for file '" + textSourceFile.getInputFile().file().getAbsolutePath() + "'. Did not check this file AT ALL.");
        return;
      }

      Pattern regexp = Pattern.compile(triggerExpression, Pattern.DOTALL);
      Matcher matcher = regexp.matcher(entireFileAsString);
      if (matcher.find()) {
//        System.out.println("Match: " + line + " on line " + lineReader.getLineNumber());
        int positionOfMatch = matcher.start();
        lineNumberOfTriggerMatch = LineNumberFinderUtil.countLines(entireFileAsString, positionOfMatch);
        triggerMatchFound = true;
      }

      regexp = Pattern.compile(mustExistExpression, Pattern.DOTALL);
      matcher = regexp.matcher(entireFileAsString);
      if (matcher.find()) {
//        System.out.println("Match: " + line + " on line " + lineReader.getLineNumber());
        mustExistMatchFound = true;
      }

      if (triggerMatchFound && !mustExistMatchFound) {
        createViolation(lineNumberOfTriggerMatch, message);
      }
    }
  }


}
