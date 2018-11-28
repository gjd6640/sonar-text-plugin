package org.sonar.plugins.text.checks;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class RequiredStringNotPresentCheckTest extends AbstractCheckTester {

  @Test
  public void largeTextFile_noScanPerformed() throws IOException {
    char[] buffer = new char[RequiredStringNotPresentCheck.MAX_CHARACTERS_SCANNED-33]; // fit the trigger expression in below the max
    Arrays.fill(buffer, 'a');

    // Set up
    super.createFileSystem();
    File tempFile1 = super.createTempFile(String.valueOf(buffer) + "prod-server-01.*prod-server-02:1000,\nprod-server-03");
    RequiredStringNotPresentCheck check = new RequiredStringNotPresentCheck();
    check.setTriggerExpression("prod-server-01.*prod-server-02:1000");
    check.setMustExistExpression("prod-server-03");

    // Run
    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertTrue(issuesFound.size() == 0);
  }

  @Test
  public void largeTextFileJustUnderLimit_scanIsPerformed() throws IOException {
    char[] buffer = new char[RequiredStringNotPresentCheck.MAX_CHARACTERS_SCANNED-150]; // fit the trigger expression in below the max
    Arrays.fill(buffer, 'a');

    // Set up
    super.createFileSystem();
    File tempFile1 = super.createTempFile(String.valueOf(buffer) + "prod-server-01.*prod-server-02:1000");
    RequiredStringNotPresentCheck check = new RequiredStringNotPresentCheck();
    check.setTriggerExpression("prod-server-01.*prod-server-02:1000");
    check.setMustExistExpression("prod-server-03");

    // Run
    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertTrue(issuesFound.size() == 1);
  }

	@Test
	public void allStringsPresent_noIssueRaised() throws IOException {
		// Set up
		super.createFileSystem();
		File tempFile1 = super.createTempFile("clusterURLs=prod-server-01:1000,\nprod-server-02:1000\nprod-server-03:1000");
		RequiredStringNotPresentCheck check = new RequiredStringNotPresentCheck();
		check.setTriggerExpression("prod-server-01.*prod-server-02:1000");
		check.setMustExistExpression("prod-server-03");

		// Run
		TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

		// Check
		List<TextIssue> issuesFound = result.getTextIssues();
		assertTrue(issuesFound.size() == 0);
	}

  @Test
  public void allStringsNotPresent_IssueRaised() throws IOException {
    // Set up
    super.createFileSystem();
    File tempFile1 = super.createTempFile("asdf\nasdf\nclusterURLs=prod-server-01:1000,\nprod-server-02:1000\n");
    RequiredStringNotPresentCheck check = new RequiredStringNotPresentCheck();
    check.setTriggerExpression("prod-server-01.*prod-server-02:1000");
    check.setMustExistExpression("prod-server-03");

    // Run
    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertTrue("Found " + issuesFound.size() + " issues", issuesFound.size() == 1);

    assertTrue(countTextIssuesFoundAtLine(3, issuesFound) == 1);
  }

  @Test
  public void allStringsNotPresent_IssueRaised_matchesAtFirstCharOnLine_verifyingLineNumberIdentification() throws IOException {
    // Set up
    super.createFileSystem();
    File tempFile1 = super.createTempFile(TEST_FILE_CONTENT);
    RequiredStringNotPresentCheck check = new RequiredStringNotPresentCheck();
    check.setTriggerExpression("PALERT:YES");
    check.setMustExistExpression("(?m)^(?!#)PROD_EMAIL:[ ]*[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z]+");

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
      "asdf\r\n";
}
