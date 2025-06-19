package org.sonar.plugins.txt.checks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.txt.TextPlugin;

public class MultilineTextMatchCheckTest extends AbstractCheckTester {

  @Test
  public void largeTextFile_noScanPerformed() throws IOException {
    char[] buffer = new char[MultilineTextMatchCheck.MAX_CHARACTERS_SCANNED-33]; // fit the trigger expression in below the max
    Arrays.fill(buffer, 'a');

    // Set up
    super.createFileSystem();
    File tempFile1 = super.createTempFile(String.valueOf(buffer) + "prod-server-01.*prod-server-02:1000,\nprod-server-03");
    MultilineTextMatchCheck check = new MultilineTextMatchCheck();
    check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
    check.setSearchRegularExpression("prod-server-01.*prod-server-02:1000");

    // Run
    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertTrue(issuesFound.size() == 0);
  }

  @Test
  public void largeTextFileJustUnderLimit_scanIsPerformed() throws IOException {
    char[] buffer = new char[MultilineTextMatchCheck.MAX_CHARACTERS_SCANNED-150]; // fit the trigger expression in below the max
    Arrays.fill(buffer, 'a');

    // Set up
    super.createFileSystem();
    File tempFile1 = super.createTempFile(String.valueOf(buffer) + "prod-server-01.*prod-server-02:1000");
    MultilineTextMatchCheck check = new MultilineTextMatchCheck();
    check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
    check.setSearchRegularExpression("prod-server-01.*prod-server-02:1000");

    // Run
    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertTrue(issuesFound.size() == 1);
  }

  @Test
  public void stringFoundInSmallFile_IssueRaised() throws IOException {
    // Set up
    super.createFileSystem();
    File tempFile1 = super.createTempFile("\n\nclusterURLs=prod-server-01:1000,\nprod-server-02:1000\n");
    MultilineTextMatchCheck check = new MultilineTextMatchCheck();
    check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
    check.setSearchRegularExpression("prod-server-01.*prod-server-02:1000");

    // Run
    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertTrue("Found " + issuesFound.size() + " issues", issuesFound.size() == 1);

    assertTrue(countTextIssuesFoundAtLine(3, issuesFound) == 1);
  }

  @Test
  public void userSuppliedChallenge_shouldMatchJustOnce() throws IOException {
    // Set up
    super.createFileSystem();
    String searchThis = "if blah\r\n" +
        "then\r\n" +
        " EXIT SCRIPT\r\n" +
        "else\r\n" +
        " blah\r\n" +
        "yep\r\n" +
        "no\r\n" +
        "asdf\r\n" +
        "EXIT SCRIPT";
    File tempFile1 = super.createTempFile(searchThis);
    MultilineTextMatchCheck check = new MultilineTextMatchCheck();
    check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
    check.setSearchRegularExpression("(?i)EXIT\\s+SCRIPT\\s*\\n*(?=[\\w])");

    // Run
    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertEquals(1, issuesFound.size());

    assertTrue("Hey, issuesFound was " + issuesFound, countTextIssuesFoundAtLine(3, issuesFound) == 1);
  }

  @Test
  public void allStringsNotPresent_IssueRaised_matchesAtFirstCharOnLine_verifyingLineNumberIdentification() throws IOException {
    // Set up
    super.createFileSystem();
    File tempFile1 = super.createTempFile(TEST_FILE_CONTENT);
    MultilineTextMatchCheck check = new MultilineTextMatchCheck();
    check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
    check.setSearchRegularExpression("PALERT:YES");

    // Run
    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertTrue("Found " + issuesFound.size() + " issues", issuesFound.size() == 1);
    assertTrue(countTextIssuesFoundAtLine(4, issuesFound) == 1);
  }

	private int countTextIssuesFoundAtLine(int lineNumber, List<TextIssue> list) {
	  int countFound = 0;
	  for (TextIssue currentIssue : list ) {
	    if (currentIssue.getLine() == lineNumber) {
	      countFound++;
	    }
	  }
	  return countFound;
	}


	 private String TEST_FILE_CONTENT = "# somecomment\r\n" +
	      "\r\n" +
	      "# PEMAIL: <<TODO: Add PEmail>>\r\n" +
	      "PALERT:YES\r\n" +
	      "asdf";
}
