package org.sonar.plugins.text.checks;

import org.sonar.api.rule.RuleKey;

public class TextIssue {

  private final RuleKey ruleKey;
  private final int line;
  private final String message;

  public TextIssue(RuleKey ruleKey, int line, String message) {
    this.ruleKey = ruleKey;
    this.line = line;
    this.message = message;
  }

  public RuleKey getRuleKey() {
    return ruleKey;
  }

  public int getLine() {
    return line;
  }

  public String getMessage() {
    return message;
  }
}
