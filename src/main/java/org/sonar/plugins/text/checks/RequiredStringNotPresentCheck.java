package org.sonar.plugins.text.checks;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.squidbridge.annotations.RuleTemplate;

@Rule(key = "RequiredStringNotPresentRegexMatchCheck", 
      priority = Priority.MAJOR, 
      name = "Required String not Present", description = "Allows you to enforce \"When string 'A' is present string 'B' must also be present\". Raises an issue when text in the file matches to some 'trigger' regular expression but none match to a 'must exist' regular expression. The regular expression evaluation uses Java's Pattern.DOTALL option so '.*' will match past newline characters.")
@RuleTemplate
public class RequiredStringNotPresentCheck extends AbstractTextCheck {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTextCheck.class);
  
  @RuleProperty(key = "triggerRegularExpression", type = "TEXT", defaultValue = "^some single-line.*regex search string$")
  private String triggerExpression;

  @RuleProperty(key = "mustExistRegularExpression", type = "TEXT", defaultValue = "^some single-line.*regex search string$")
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

  public void setTriggerExpression(String expression) {
    this.triggerExpression = expression;
  }

  public void setMustExistExpression(String mustExistExpression) {
    this.mustExistExpression = mustExistExpression;
  }

  public void setFilePattern(String filePattern) {
    this.filePattern = filePattern;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  protected static final int MAX_CHARACTERS_SCANNED = 500001;
  
  @Override
  public void validate(TextSourceFile textSourceFile, String projectKey) {
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
      String entireFileAsString = readFileAsString(path);
      
      Pattern regexp = Pattern.compile(triggerExpression, Pattern.DOTALL);
      Matcher matcher = regexp.matcher(entireFileAsString);
      if (matcher.find()) {
//        System.out.println("Match: " + line + " on line " + lineReader.getLineNumber());
        int positionOfMatch = matcher.start();
        lineNumberOfTriggerMatch = countLines(entireFileAsString, positionOfMatch);
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
  
  private String readFileAsString(Path path) {
    CharBuffer fileContentBuffer = CharBuffer.allocate(MAX_CHARACTERS_SCANNED);

    try ( BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8) ) {
      int result = reader.read(fileContentBuffer);
      if (result == MAX_CHARACTERS_SCANNED) {
        System.out.println("Text scanner (RequiredStringNotPresentRegexMatchCheck) maximum scan depth ( " + (MAX_CHARACTERS_SCANNED-1) + " chars) encountered for file '" + path.toFile().getAbsolutePath() + "'. Did not check this file AT ALL.");
        return "";
      } else {
        fileContentBuffer.flip();
      }
      
    } catch (BufferOverflowException ex) {
      
    } catch (IOException ex){
      throw new RuntimeException(ex);
    }
    
    return fileContentBuffer.toString();
  }
  
  private int countLines(String str, int stopAtPosition) {
    if(str == null || str.isEmpty())
    {
        return 0;
    }
    int lines = 1;
    int pos = 0;
    while ((pos = str.indexOf("\n", pos) + 1) != 0 && pos < stopAtPosition) {
        lines++;
    }
    return lines;
}
}
