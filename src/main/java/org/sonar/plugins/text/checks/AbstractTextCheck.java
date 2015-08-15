package org.sonar.plugins.text.checks;

import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.WildcardPattern;

public abstract class AbstractTextCheck {

  private RuleKey ruleKey;
  private TextSourceFile textSourceFile;

  protected final void createViolation(Integer linePosition, String message) {
	  textSourceFile.addViolation(new TextIssue(ruleKey, linePosition, message));
  }

  /**
   * Apply the Ant style file pattern to decide if the file is included
   */
  protected boolean isFileIncluded(String filePattern) {
    if (filePattern != null) {
      return WildcardPattern.create(filePattern)
        .match(textSourceFile.getLogicalPath());

    } else {
      return true;
    }
  }

  public final void setRuleKey(RuleKey ruleKey) {
    this.ruleKey = ruleKey;
  }

  protected void setTextSourceFile(TextSourceFile sourceFile) {
    this.textSourceFile = sourceFile;
  }

  public abstract void validate(TextSourceFile sourceFile);
}
