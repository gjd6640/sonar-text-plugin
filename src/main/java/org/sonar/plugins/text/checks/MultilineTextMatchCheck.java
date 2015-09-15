package org.sonar.plugins.text.checks;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.squidbridge.annotations.RuleTemplate;

@Rule(key = "MultilineTextMatchCheck", 
      priority = Priority.MAJOR, 
      name = "Multiline Regex Check", description = "Multiline (Java Match.DOTALL) regular expression matcher. Scans only text files containing less than " + (MultilineTextMatchCheck.MAX_CHARACTERS_SCANNED-1) + " characters.")
@RuleTemplate
public class MultilineTextMatchCheck extends AbstractTextCheck {
  @RuleProperty(key = "regularExpression", type = "TEXT", defaultValue = "^some.*regex search string\\. dot matches all$")
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

  public void setSearchRegularExpression(String expression) {
    this.searchRegularExpression = expression;
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
    int lineNumberOfTriggerMatch = -1;

    setTextSourceFile(textSourceFile);

    if (searchRegularExpression != null &&
        isFileIncluded(filePattern) &&
        shouldFireForProject(projectKey) && 
        shouldFireOnFile(textSourceFile.getInputFile()) 
        ) {
    	
      Path path = textSourceFile.getInputFile().file().toPath();
      String entireFileAsString = readFileAsString(path);
      
      Pattern regexp = Pattern.compile(searchRegularExpression, Pattern.DOTALL);
      Matcher matcher = regexp.matcher(entireFileAsString);
      if (matcher.find()) {
//        System.out.println("Match: " + line + " on line " + lineReader.getLineNumber());
        int positionOfMatch = matcher.start();
        lineNumberOfTriggerMatch = countLines(entireFileAsString, positionOfMatch);
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
    if(str == null || str.isEmpty()) {
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
