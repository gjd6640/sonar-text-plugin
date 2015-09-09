package org.sonar.plugins.text.checks;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

public class SimpleTextMatchCheckTest extends AbstractCheckTester {

	@Test
	public void simpleCase_successfulMatch() throws IOException {
		// Set up
		super.createFileSystem();
		File tempFile1 = super.createTempFile("objectionable string\n\nsadf\n\n1objectionable string");
		SimpleTextMatchCheck check = new SimpleTextMatchCheck();
		check.setExpression(".*objectionable string.*");
		
		// Run
		TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");
		
		// Check
		List<TextIssue> issuesFound = result.getTextIssues();
		assertTrue(issuesFound.size() == 2);
		
		assertTrue(countTextIssuesFoundAtLine(1, issuesFound) == 1);
		assertTrue(countTextIssuesFoundAtLine(5, issuesFound) == 1);
	}

	@Test
	public void ProjectNameExclusionApplies_matchesIgnored() throws IOException {
		// Set up
		super.createFileSystem();
		File tempFile1 = super.createTempFile("objectionable string\n\nsadf\n\n1objectionable string");
		SimpleTextMatchCheck check = new SimpleTextMatchCheck();
		check.setExpression(".*objectionable string.*");
		check.setDoNotFireForProjectKeysRegex(".*do-SPECIAL_THING.*");
		// Run
		TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-SPECIAL_THING-blah-blah");
		
		// Check
		List<TextIssue> issuesFound = result.getTextIssues();
		assertTrue(issuesFound.size() == 0);
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
	
}
