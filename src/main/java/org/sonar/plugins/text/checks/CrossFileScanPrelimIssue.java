package org.sonar.plugins.text.checks;

import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.text.checks.AbstractCrossFileCheck.RulePart;

public class CrossFileScanPrelimIssue extends TextIssue {

  private final RulePart rulePart;

  /**
   *
   * @param ruleKey
   * @param line
   * @param message
   * @param rulePart The portion of the multipart check that matched to a line of a file. This dataset will be used in the final analysis/issue raising step to determine which pieces of the rule matched in order to decide:
   *         1) whether to raise an issue
   *           and
   *         2) Which "rulePart" to raise as an issue. We'll have the flexibility to use the first match pattern as a trigger to enable raising an issue only on occurrences of the second match pattern OR we can raise issues for matches on both patterns.
   */
  public CrossFileScanPrelimIssue(final RulePart rulePart, final RuleKey ruleKey, final int line, final String message) {
    super(ruleKey, line, message);
    this.rulePart = rulePart;
  }

  public RulePart getRulePart() {
    return rulePart;
  }

  @Override
  public String toString() {
    return "CrossFileScanPrelimIssue [rulePart=" + rulePart + ", toString()=" + super.toString() + "]";
  }

}
