package org.sonar.plugins.text.checks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.squidbridge.annotations.RuleTemplate;

@Rule(key = "StringDisallowedIfMatchInAnotherFileCheck",
      priority = Priority.MAJOR,
      name = "String disallowed if a match was found in another file", description = "Checks for a 'trigger match' in one file. Only if that is present a second expression is checked against all files in the project and all matches cause an issue to be raised. Regex is applied in simple non-DOTALL mode / is single-line-based.")
@RuleTemplate
public class StringDisallowedIfMatchInAnotherFileCheck extends AbstractCrossFileCheck {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTextCheck.class);

  @RuleProperty(key = "triggerExpression", type = "TEXT", defaultValue = "^some single-line.*regex search string$")
  private String triggerExpression;

  @RuleProperty(key = "triggerFilePattern", defaultValue = "**/*.properties", description = "Ant Style path expression. To include all of the files in this project use '**/*'. \n\nFiles scanned will be limited by the list of file extensions configured for this language AND by the values of 'sonar.sources' and 'sonar.exclusions'. Also, using just 'filename.txt' here to point the rule to a file at the root of the project does not appear to work (as of SQ v4.5.5). Use '**/filename.txt' instead.")
  private String triggerFilePattern;

  @RuleProperty(key = "disallowExpression", type = "TEXT", defaultValue = "^some single-line.*regex search string$")
  private String disallowExpression;

  @RuleProperty(key = "disallowFilePattern", defaultValue = "**/*.properties", description = "Ant Style path expression. To include all of the files in this project use '**/*'. \n\nFiles scanned will be limited by the list of file extensions configured for this language AND by the values of 'sonar.sources' and 'sonar.exclusions'. Also, using just 'filename.txt' here to point the rule to a file at the root of the project does not appear to work (as of SQ v4.5.5). Use '**/filename.txt' instead.")
  private String disallowFilePattern;

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
//    System.out.println("Current file: " + textSourceFile.getInputFile().absolutePath());
//LOG.info("validating");
    if (triggerExpression != null &&
        isFileIncluded(triggerFilePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {
//      System.out.println("Checking file: " + textSourceFile.getInputFile().absolutePath());

      Pattern regexp = Pattern.compile(triggerExpression);
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
//    	          System.out.println("Trigger match: " + line + " on line " + lineReader.getLineNumber());
    	          recordMatch(RulePart.TriggerPattern, lineReader.getLineNumber(), message);
//    	          break;
    	        }
    	      }
    	    }
    	    catch (IOException ex){
    	      throw new RuntimeException(ex);
    	    }
    } else {
//      System.out.println("Did not check file '" + textSourceFile.getInputFile().absolutePath() + "' for trigger.");
    }

    if (disallowExpression != null &&
        isFileIncluded(disallowFilePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {

      Pattern regexp = Pattern.compile(disallowExpression);
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
//                System.out.println("Disallow match: " + line + " on line " + lineReader.getLineNumber());
                recordMatch(RulePart.DisallowPattern, lineReader.getLineNumber(), message);
              }
            }
          }
          catch (IOException ex){
            throw new RuntimeException(ex);
          }
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


}
