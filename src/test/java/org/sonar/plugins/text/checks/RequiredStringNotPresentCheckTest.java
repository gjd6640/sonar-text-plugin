package org.sonar.plugins.text.checks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

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
    check.setTriggerExpression("PROD_ALERT:YES");
    check.setMustExistExpression("(?m)^(?!#)PROD_EMAIL:[ ]*[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.[a-zA-Z]+");
    
    // Run
    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");
    
    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertTrue("Found " + issuesFound.size() + " issues", issuesFound.size() == 1);
    assertTrue(countTextIssuesFoundAtLine(20, issuesFound) == 1);
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
	
	private String TEST_FILE_CONTENT = "# Server fault monitoring properties\r\n" + 
	    "# LOG: <Complete path to Logfile> server-fault.log file path.\r\n" + 
	    "# SEARCH STRING: <Search string> String to be searched in server-fault.log file\r\n" + 
	    "# DEV_EMAIL: <Email Id> Support team alert email Id for dev env\r\n" + 
	    "# TEST_EMAIL: <Email Id> Support team alert email Id for test env\r\n" + 
	    "# PROD_EMAIL: <Email Id> Support team alert email Id for prod env\r\n" + 
	    "# Only PROD_ALERT should be YES,  YES is to send  alerts to OSGCC, default value is NO.\r\n" + 
	    "# ------------------------------------------------------------------\r\n" + 
	    "\r\n" + 
	    "LOG:/logs/jhe/netcontrol/nti/iris-TCS-service-iris-create-train-in-netcontrol-1_0/server-fault.log\r\n" + 
	    "SEARCH STRING:<faultcode>Server\r\n" + 
	    "\r\n" + 
	    "# DEV_EMAIL: <<TODO: Add logScraperDevEmail>>\r\n" + 
	    "DEV_ALERT:NO\r\n" + 
	    "\r\n" + 
	    "# TEST_EMAIL: <<TODO: Add logScraperTestEmail>>\r\n" + 
	    "TEST_ALERT:NO\r\n" + 
	    "\r\n" + 
	    "# PROD_EMAIL: <<TODO: Add logScraperProdEmail>>\r\n" + 
	    "PROD_ALERT:YES\r\n" + 
	    "asdf";
	
}
