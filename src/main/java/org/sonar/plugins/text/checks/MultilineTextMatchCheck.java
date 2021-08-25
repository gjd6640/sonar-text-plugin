package org.sonar.plugins.text.checks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.text.checks.util.FileIOUtil;
import org.sonar.plugins.text.checks.util.LargeFileEncounteredException;
import org.sonar.plugins.text.checks.util.LineNumberFinderUtil;

@Rule(key = "MultilineTextMatchCheck",
      priority = Priority.MAJOR,
      name = "Multiline Regex Check", description = "Multiline (Java Match.DOTALL) regular expression matcher. Scans only text files containing less than " + (MultilineTextMatchCheck.MAX_CHARACTERS_SCANNED-1) + " characters. Note that ^ and $ character matching is to beginning and end of file UNLESS you start your expression with (?m).")
public class MultilineTextMatchCheck extends AbstractTextCheck {
  @RuleProperty(key = "regularExpression", type = "TEXT", defaultValue = "(?m)^some.*regex search string\\. dot matches all$")
  private String searchRegularExpression;

  @RuleProperty(key = "filePattern", defaultValue = "**/*.properties", description = "Ant Style path expression. To include all of the files in this project use '**/*'. \n\nFiles scanned will be limited by the list of file extensions configured for this language AND by the values of 'sonar.sources' and 'sonar.exclusions'. Also, using just 'filename.txt' here to point the rule to a file at the root of the project does not appear to work (as of SQ v4.5.5). Use '**/filename.txt' instead.")
  private String filePattern;

  @RuleProperty(
    key = "message")
  private String message;

  public String getExpression() {
    return searchRegularExpression;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public String getMessage() {
    return message;
  }

  public void setSearchRegularExpression(final String expression) {
    this.searchRegularExpression = expression;
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
    int lineNumberOfTriggerMatch = -1;

    setTextSourceFile(textSourceFile);

    if (searchRegularExpression != null &&
        isFileIncluded(filePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {

      String entireFileAsString;
      try {
        entireFileAsString = FileIOUtil.readFileAsString(textSourceFile, MAX_CHARACTERS_SCANNED);
      } catch (LargeFileEncounteredException ex) {
        // The util class logs the fact that we're skipping this file...
        return;
      }

      Pattern regexp = Pattern.compile(searchRegularExpression, Pattern.DOTALL);
      Matcher matcher = regexp.matcher(entireFileAsString);
      if (matcher.find()) {
//        System.out.println("Match: " + line + " on line " + lineReader.getLineNumber());
        int positionOfMatchBegin = matcher.start();
//        int positionOfMatchEnd = matcher.end();
        lineNumberOfTriggerMatch = LineNumberFinderUtil.countLines(entireFileAsString, positionOfMatchBegin);
        createViolation(lineNumberOfTriggerMatch, message);
      }

    }
  }

}
