package org.sonar.plugins.txt.checks;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.txt.TextPlugin;

public class SimpleTextMatchCheckTest extends AbstractCheckTester {

	@Test
	public void simpleCase_successfulMatch() throws IOException {
		// Set up
		//super.createFileSystem();
		File tempFile1 = super.createTempFile("objectionable string\n\nsadf\n\n1objectionable string");
		SimpleTextMatchCheck check = new SimpleTextMatchCheck();
		check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
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
  public void simpleCase_ignoresCharactersNotInCharset() throws IOException {
    // Set up
    super.createFileSystem();
    String sep = File.separator;
    File jsFile = new File("src" + sep + "test" + sep + "resources" + sep + "invalidCharacterBytes.js");

    SimpleTextMatchCheck check = new SimpleTextMatchCheck();
    check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
    check.setExpression(".*(ts_sort_numeric|ts_sort_datetime).*");

    // Run
    TextSourceFile result = parseAndCheck(jsFile, check, "com.mycorp.projectA.service:service-do-X");

    // Check
    List<TextIssue> issuesFound = result.getTextIssues();
    assertTrue(issuesFound.size() == 2);

    assertTrue(countTextIssuesFoundAtLine(1, issuesFound) == 1);
    assertTrue(countTextIssuesFoundAtLine(4, issuesFound) == 1);
  }

	 @Test
	  public void simpleCaseInvolvingStartOfLineCharacter_usesStartOfEachLine() throws IOException {
	    // Set up
	    super.createFileSystem();
	    File tempFile1 = super.createTempFile("objectionable string\n\nsadf\n\nobjectionable string");
	    SimpleTextMatchCheck check = new SimpleTextMatchCheck();
	    check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
	    check.setExpression("^objectionable string");

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
		check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
		check.setExpression(".*objectionable string.*");
		check.setDoNotFireForProjectKeysRegex(".*do-SPECIAL_THING.*");
		// Run
		TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-SPECIAL_THING-blah-blah");

		// Check
		List<TextIssue> issuesFound = result.getTextIssues();
		assertTrue(issuesFound.size() == 0);
	}

	 @Test
	  public void FileNameExclusionApplies_matchesIgnored() throws IOException {
	    // Set up
	    super.createFileSystem();
	    File tempFile1 = super.createTempFile("objectionable string\n\nsadf\n\n1objectionable string");
	    SimpleTextMatchCheck check = new SimpleTextMatchCheck();
	    check.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY, "someRuleKey_for_class_" + check.getClass().getName()));
	    check.setExpression(".*objectionable string.*");
	    check.setDoNotFireForProjectKeysRegex(".*do-SPECIAL_THING.*"); // should have no effect here
	    check.setDoNotFireForTheseFileNamesRegex("file.xml"); // this is the file name that 'super.createFileSystem()' sets so this will have an effect here

	    // Run
	    TextSourceFile result = parseAndCheck(tempFile1, check, "com.mycorp.projectA.service:service-do-X");

	    // Check
	    List<TextIssue> issuesFound = result.getTextIssues();
	    assertTrue(issuesFound.size() == 0);
	  }

	private int countTextIssuesFoundAtLine(final int lineNumber, final List<TextIssue> list) {
	  int countFound = 0;
	  for (TextIssue currentIssue : list ) {
  		if (currentIssue.getLine() == lineNumber) {
  			countFound++;
  		}
	  }
	  return countFound;
	}

}
