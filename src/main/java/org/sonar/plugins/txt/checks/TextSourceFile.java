package org.sonar.plugins.txt.checks;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.batch.fs.InputFile;

public class TextSourceFile {

  private final List<TextIssue> textIssues = new ArrayList<TextIssue>();

  private InputFile inputFile;

  /**
   * Used for tracking violations on each scanned file
   */
  public TextSourceFile(InputFile file) {
    this.inputFile = file;
  }

  public void addViolation(TextIssue textIssue) {
    this.textIssues.add(textIssue);
  }

  public InputFile getInputFile() {
    return inputFile;
  }

  public String getLogicalPath() {
    return inputFile.absolutePath();
  }

  public List<TextIssue> getTextIssues() {
    return textIssues;
  }

  @Override
  public String toString() {
    return inputFile.absolutePath();
  }
}
