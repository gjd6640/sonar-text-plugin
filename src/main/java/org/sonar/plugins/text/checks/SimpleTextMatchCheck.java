package org.sonar.plugins.text.checks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "SimpleRegexMatchCheck",
      priority = Priority.MAJOR,
      name = "Simple Regex Match", description = "Simple regular expression matcher.")
public class SimpleTextMatchCheck extends AbstractTextCheck {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTextCheck.class);

  @RuleProperty(key = "expression", type = "TEXT", defaultValue = "^some single-line.*regex search string$", description = "Don't try to match to newlines (\\r or \\n); consider using the Multiline check type if you have that need. This rule type evaluates your pattern against a single line of text at a time and uses java.io.LineNumberReader.readLine() to obtain that text. That method consumes new line information but does not return it.")
  private String expression;

  @RuleProperty(key = "filePattern", defaultValue = "**/*.properties", description = "Ant Style path expression. To include all of the files in this project use '**/*'. \n\nFiles scanned will be limited by the list of file extensions configured for this language AND by the values of 'sonar.sources' and 'sonar.exclusions'. Also, using just 'filename.txt' here to point the rule to a file at the root of the project does not appear to work (as of SQ v4.5.5). Use '**/filename.txt' instead.")
  private String filePattern;

  @RuleProperty(
    key = "message")
  private String message;

  public String getExpression() {
    return expression;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public String getMessage() {
    return message;
  }

  public void setExpression(final String expression) {
    this.expression = expression;
  }

  public void setFilePattern(final String filePattern) {
    this.filePattern = filePattern;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public void validate(final TextSourceFile textSourceFile, final String projectKey) {
    setTextSourceFile(textSourceFile);
//LOG.info("validating");
    if (expression != null &&
        isFileIncluded(filePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {

      Pattern regexp = Pattern.compile(expression);
      Matcher matcher = regexp.matcher(""); // Apply the pattern to search this empty string just to get a matcher reference. We'll reset it in a moment to work against a real string.

      File inputFile = textSourceFile.getInputFile().file();
      CharsetDecoder decoder = (StandardCharsets.UTF_8).newDecoder();
      decoder.onMalformedInput(CodingErrorAction.IGNORE);

      try (LineNumberReader lineReader = new LineNumberReader(new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), decoder)));
          ) {
    	      String line = null;
    	      while ((line = lineReader.readLine()) != null) {
    	        matcher.reset(line); // Reuse the matcher by discarding its current state and providing new input text
    	        if (matcher.find()) {
//    	          System.out.println("Match: " + line + " on line " + lineReader.getLineNumber());
    	          createViolation(lineReader.getLineNumber(), message);
    	        }
    	      }
    	    }
    	    catch (IOException ex){
    	      throw new RuntimeException(ex);
    	    }
    }
  }
}
