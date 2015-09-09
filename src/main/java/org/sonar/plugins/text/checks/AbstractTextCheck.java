package org.sonar.plugins.text.checks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.check.RuleProperty;

public abstract class AbstractTextCheck {

  private RuleKey ruleKey;
  private TextSourceFile textSourceFile;

  @RuleProperty(key = "doNotFireForTheseProjectKeys", type = "TEXT", defaultValue = "", description = "Use to exclude certain projects from this rule. Sample pattern: '.*key portion of the project keys for which this rule must not apply.*'")
  private String doNotFireForProjectKeysRegex;

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

  protected boolean shouldFireForProject(String currentProjectKey) {
    if (doNotFireForProjectKeysRegex == null || "".equals(doNotFireForProjectKeysRegex.trim())) {
      return true;
    } else {
  	  Pattern regexp = Pattern.compile(doNotFireForProjectKeysRegex);
      Matcher matcher = regexp.matcher(currentProjectKey);
      return !matcher.find();
    }
  }
  
  public final void setRuleKey(RuleKey ruleKey) {
    this.ruleKey = ruleKey;
  }
  
  public void setDoNotFireForProjectKeysRegex(String doNotFireForProjectKeysRegex) {
	this.doNotFireForProjectKeysRegex = doNotFireForProjectKeysRegex;
  }

  protected void setTextSourceFile(TextSourceFile sourceFile) {
    this.textSourceFile = sourceFile;
  }

  public abstract void validate(TextSourceFile sourceFile, String projectKey);

}
