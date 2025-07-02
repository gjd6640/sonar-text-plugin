package org.sonar.plugins.txt.checks;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.plugins.txt.TextPlugin;
import org.sonar.plugins.txt.batch.TextIssueSensor;
import org.sonar.plugins.txt.checks.AbstractCrossFileCheck.RulePart;
import org.sonar.plugins.txt.testutils.FileTestUtils;

public class StringDisallowedIfMatchInAnotherFileCheckTest extends AbstractCrossFileCheckTester {
  final private Path tempInputFilesPath = Paths.get("target/surefire-test-resources/StringDisallowedIfMatchInAnotherFileCheckTest/");

  private DefaultFileSystem fs;
  private TextIssueSensor sensor;
  private final StringDisallowedIfMatchInAnotherFileCheck realStringDisallowedMultiFileCheck = new StringDisallowedIfMatchInAnotherFileCheck();

  @Mock private InputProject project;
  private SensorContextTester sensorContext;

  @Before
  public void setUp() throws Exception {
    Files.createDirectories(tempInputFilesPath);
    sensorContext = SensorContextTester.create(tempInputFilesPath);
    fs = sensorContext.fileSystem();

  }

  // Code originally borrowed from IssueSensorTest...
  // This is a bit of an integration test as it wires up the TextIssueSensor. This enables us to know that these classes play nice together.
  @Test
  public void analyse_multi_class_integration_test() throws IOException {
    // Setup

    // Create files to be scanned

    // File containing trigger pattern
    Path inputFilePath = Paths.get(tempInputFilesPath.toString(), "effective-pom.xml");
    DefaultInputFile inputFile = FileTestUtils.createInputFile(inputFilePath, String.join("\n", Arrays.asList("The first line", "<target>1.8</target>", "The third line")));
    fs.add(inputFile);

    // File with disallowed config
    inputFilePath = Paths.get(tempInputFilesPath.toString(), "feature-setup-env.properties");
    inputFile = FileTestUtils.createInputFile(inputFilePath, String.join("\n", Arrays.asList("The first line", "JAVA_HOME=/software/java64/jdk1.7.0_60", "The third line")));
    fs.add(inputFile);

    // Configure the check
    realStringDisallowedMultiFileCheck.setTriggerFilePattern("**/effective-pom.xml");
    realStringDisallowedMultiFileCheck.setTriggerExpression(".*<target>1.8</target>.*");
    realStringDisallowedMultiFileCheck.setDisallowFilePattern("**/*setup-env*");
    realStringDisallowedMultiFileCheck.setDisallowExpression(".*JAVA_HOME=.*jdk1.(6|7).*");
    realStringDisallowedMultiFileCheck.setApplyExpressionToOneLineOfTextAtATime(true);

    realStringDisallowedMultiFileCheck.setMessage("Project compiled to target Java 8 is being booted under a prior JVM version.");

    // Run
    sensor.execute(sensorContext);

    // Verify
    assertEquals(1, sensorContext.allIssues().size());

    Issue issueRaised = sensorContext.allIssues().iterator().next();
    assertEquals(RuleKey.of(TextPlugin.REPOSITORY_KEY, "StringDisallowedIfMatchInAnotherFileCheck"), issueRaised.ruleKey());
  }

  @Test
  public void analyse_multi_class_integration_test_multiline_aka_DOTALL_regex() throws IOException {
    // Setup: Create files to be scanned

    // File containing trigger pattern
    Path inputFilePath = Paths.get(tempInputFilesPath.toString(), "effective-pom.xml");
    DefaultInputFile inputFile = FileTestUtils.createInputFile(inputFilePath, String.join("\n", Arrays.asList("The first line", "<target>1.8</target>", "The third line")));
    fs.add(inputFile);

    // File with disallowed config
    inputFilePath = Paths.get(tempInputFilesPath.toString(), "feature-setup-env.properties");
    inputFile = FileTestUtils.createInputFile(inputFilePath, String.join("\n", Arrays.asList("The first line", "JAVA_HOME=/software/java64/jdk1.7.0_60", "The third line")));
    fs.add(inputFile);

    // Configure the check
    realStringDisallowedMultiFileCheck.setTriggerFilePattern("**/effective-pom.xml");
    realStringDisallowedMultiFileCheck.setTriggerExpression("(?s).*<target>1.8</target>.*third");
    realStringDisallowedMultiFileCheck.setDisallowFilePattern("**/*setup-env*");
    realStringDisallowedMultiFileCheck.setDisallowExpression("(?s).*JAVA_HOME=.*jdk1.(6|7).*third");
    realStringDisallowedMultiFileCheck.setApplyExpressionToOneLineOfTextAtATime(false);

    realStringDisallowedMultiFileCheck.setMessage("Project compiled to target Java 8 is being booted under a prior JVM version.");

    // Run
    sensor.execute(sensorContext);

    // Verify
    assertEquals(1, sensorContext.allIssues().size());

    Issue issueRaised = sensorContext.allIssues().iterator().next();
    assertEquals(RuleKey.of(TextPlugin.REPOSITORY_KEY, "StringDisallowedIfMatchInAnotherFileCheck"), issueRaised.ruleKey());
  }

  @Test
  public void recordMatchTest(){
    // Set up
    StringDisallowedIfMatchInAnotherFileCheck chk = new StringDisallowedIfMatchInAnotherFileCheck();
    Map<InputFile, List<CrossFileScanPrelimIssue>> rawResults = new HashMap<>();
    chk.setCrossFileChecksRawResults(rawResults);

    // Execute
    chk.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY,"rule1"));
//    Path inputFilePath = Paths.get(tempInputFilesPath.toString(), "effective-pom.xml");
    chk.setTextSourceFile(new TextSourceFile(FileTestUtils.createInputFileShell(tempInputFilesPath.toString() + "somepath")));
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");

    chk.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY,"rule2"));
    chk.setTextSourceFile(new TextSourceFile(FileTestUtils.createInputFileShell(tempInputFilesPath.toString() + "someOtherPath")));
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");

    // Verify
    Assert.assertTrue(rawResults.size() == 2);

    List<CrossFileScanPrelimIssue> issuesForOneFile = rawResults.get(FileTestUtils.createInputFileShell(tempInputFilesPath.toString() + "somepath"));
    Assert.assertTrue(issuesForOneFile.size() == 2);

    issuesForOneFile = rawResults.get(FileTestUtils.createInputFileShell(tempInputFilesPath.toString() + "someOtherPath"));
    Assert.assertTrue(issuesForOneFile.size() == 2);
  }

  @Test
  public void raiseIssuesAfterScanTest() {
    // Set up
    StringDisallowedIfMatchInAnotherFileCheck chk = new StringDisallowedIfMatchInAnotherFileCheck();
    Map<InputFile, List<CrossFileScanPrelimIssue>> rawResults = new HashMap<>();

    chk.setCrossFileChecksRawResults(rawResults);
    List<CrossFileScanPrelimIssue> issuesForOneFile = new LinkedList<>();
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.TriggerPattern, RuleKey.of(TextPlugin.REPOSITORY_KEY,"rule1"), 1, "msg"));
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.DisallowPattern, RuleKey.of(TextPlugin.REPOSITORY_KEY,"rule1"), 1, "msg"));
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.DisallowPattern, RuleKey.of(TextPlugin.REPOSITORY_KEY,"rule2"), 1, "msg"));  // not triggered, should not raise an issue
    rawResults.put(FileTestUtils.createInputFileShell(tempInputFilesPath.toString() + "file1"), issuesForOneFile);

    // Execute
    chk.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY,"rule1"));
    List<TextSourceFile> rule1Results = chk.raiseIssuesAfterScan();

    chk.setRuleKey(RuleKey.of(TextPlugin.REPOSITORY_KEY,"rule2"));
    List<TextSourceFile> rule2Results = chk.raiseIssuesAfterScan();

    // Verify
    Assert.assertTrue(rule1Results.size() == 1);
    Assert.assertTrue(rule2Results.size() == 0);

  }

  @Before
  public void createIssueSensorBackedByMocks() {
    Checks<Object> checks = mock(Checks.class);
    CheckFactory checkFactory = mock(CheckFactory.class);
    when(checkFactory.create(Mockito.anyString())).thenReturn(checks);
    when(checks.addAnnotatedChecks(Mockito.any(Iterable.class))).thenReturn(checks);
    List<Object> checksList = Arrays.asList(new Object[] {realStringDisallowedMultiFileCheck});
    when(checks.all()).thenReturn(checksList);

    when(checks.ruleKey(Mockito.isA(StringDisallowedIfMatchInAnotherFileCheck.class))).thenReturn(RuleKey.of(TextPlugin.REPOSITORY_KEY, "StringDisallowedIfMatchInAnotherFileCheck"));

    sensor = new TextIssueSensor(fs, sensorContext, checkFactory);
  }

}
