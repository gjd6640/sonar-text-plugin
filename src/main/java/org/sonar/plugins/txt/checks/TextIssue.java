package org.sonar.plugins.txt.checks;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.rule.RuleKey;

public class TextIssue {

  private final RuleKey ruleKey;
  private final int line;
  private final String message;

  public TextIssue(final RuleKey ruleKey, final int line, final String message) {
    this.ruleKey = ruleKey;
    this.line = line;
    if (StringUtils.isBlank(message)) {
      // Sending a null message to the sonarqube API when we later raise an issue will cause an NPE in the scanner. Here
      // we substitute something when there's no message value.
      this.message = String.format("Rule '%s:%s' violated. (rule has no message defined)", ruleKey.repository(), ruleKey.rule());
    } else {
      this.message = message;
    }
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

  @Override
  public String toString() {
    return "TextIssue [ruleKey=" + ruleKey + ", line=" + line + ", message=" + message + "]";
  }
}
