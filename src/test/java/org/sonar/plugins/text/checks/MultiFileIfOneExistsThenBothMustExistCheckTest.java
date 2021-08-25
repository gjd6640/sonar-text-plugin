package org.sonar.plugins.text.checks;

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
import org.sonar.plugins.text.batch.TextIssueSensor;
import org.sonar.plugins.text.checks.AbstractCrossFileCheck.RulePart;
import org.sonar.plugins.text.testutils.FileTestUtils;

public class MultiFileIfOneExistsThenBothMustExistCheckTest extends AbstractCrossFileCheckTester {
  final private Path tempInputFilesPath = Paths.get("target/surefire-test-resources/MultiFileIfOneExistsThenBothMustExistCheckTest/");

  @Mock private InputProject project;
  private SensorContextTester sensorContext;

  private DefaultFileSystem fs;
  private TextIssueSensor sensor;
  private final MultiFileIfOneStringExistsThenBothMustExistCheck realIfOneStringExistsBothMustExistMultiFileCheck = new MultiFileIfOneStringExistsThenBothMustExistCheck();

  @Before
  public void setUp() throws Exception {
    Files.createDirectories(tempInputFilesPath);
    sensorContext = SensorContextTester.create(tempInputFilesPath);
    fs = sensorContext.fileSystem();
  }

  // Code originally borrowed from IssueSensorTest...
  // This is a bit of an integration test as it wires up the TextIssueSensor. This enables us to know that these classes play nice together.
  @Test
  public void execute_multi_class_integration_test() throws IOException {
    // Create files to be scanned
    // File containing trigger pattern
    Path inputFilePath = Paths.get(tempInputFilesPath.toString(), "effective-pom.xml");
    DefaultInputFile inputFile = FileTestUtils.createInputFile(inputFilePath, "The first line\n<target>1.8</target>\nThe third line");
    fs.add(inputFile);

    // File with disallowed config
    inputFilePath = Paths.get(tempInputFilesPath.toString(), "feature-setup-env.properties");
    inputFile = FileTestUtils.createInputFile(inputFilePath, String.join("\n", Arrays.asList("The first line", "JAVA_HOME=/software/java64/jdk1.7.0_60", "The third line")));
    fs.add(inputFile);

    // Configure the check
    realIfOneStringExistsBothMustExistMultiFileCheck.setTriggerFilePattern("**/effective-pom.xml");
    realIfOneStringExistsBothMustExistMultiFileCheck.setTriggerExpression(".*<target>1.8</target>.*");
    realIfOneStringExistsBothMustExistMultiFileCheck.setMustAlsoExistFilePattern("**/*setup-env*");
    realIfOneStringExistsBothMustExistMultiFileCheck.setMustAlsoExistExpression(".*-DFooProperty");
    realIfOneStringExistsBothMustExistMultiFileCheck.setApplyExpressionToOneLineOfTextAtATime(true);

    realIfOneStringExistsBothMustExistMultiFileCheck.setMessage("Project compiled to target Java 8 doesn't have recommended system property 'FooProperty'.");

    // Run
    sensor.execute(sensorContext);

    // Verify

    assertEquals(1, sensorContext.allIssues().size());

    Issue issueRaised = sensorContext.allIssues().iterator().next();
    assertEquals(RuleKey.of("text", "MultiFileIfOneStringExistsThenBothMustExistCheck"), issueRaised.ruleKey());
    assertEquals(".:target/surefire-test-resources/MultiFileIfOneExistsThenBothMustExistCheckTest/effective-pom.xml", issueRaised.primaryLocation().inputComponent().key());
    assertEquals("Project compiled to target Java 8 doesn't have recommended system property 'FooProperty'.", issueRaised.primaryLocation().message());
    assertEquals(2, issueRaised.primaryLocation().textRange().start().line());
  }

  @Test
  public void execute_multi_class_integration_test_multiline_aka_DOTALL_regex() throws IOException {
    // Setup

      // Create files to be scanned
      // File containing trigger pattern
      Path sampleFilePath = Paths.get(tempInputFilesPath.toString(), "effective-pom.xml");
      DefaultInputFile inputFile = FileTestUtils.createInputFile(sampleFilePath, "The first line\n<target>1.8</target>\nThe third line");
      fs.add(inputFile);

      // File with disallowed config
      sampleFilePath = Paths.get(tempInputFilesPath.toString(), "feature-setup-env.properties");
      inputFile = FileTestUtils.createInputFile(sampleFilePath, "The first line\nJAVA_HOME=/software/java64/jdk1.7.0_60\nThe third line");
      fs.add(inputFile);

      // Configure the check
      realIfOneStringExistsBothMustExistMultiFileCheck.setTriggerFilePattern("**/effective-pom.xml");
      realIfOneStringExistsBothMustExistMultiFileCheck.setTriggerExpression("(?s).*<target>1.8</target>.*third");
      realIfOneStringExistsBothMustExistMultiFileCheck.setMustAlsoExistFilePattern("**/*setup-env*");
      realIfOneStringExistsBothMustExistMultiFileCheck.setMustAlsoExistExpression("(?s).*-DFooProperty.*third");
      realIfOneStringExistsBothMustExistMultiFileCheck.setApplyExpressionToOneLineOfTextAtATime(false);

      realIfOneStringExistsBothMustExistMultiFileCheck.setMessage("Project compiled to target Java 8 is being booted under a prior JVM version.");

    // Run
    sensor.execute(sensorContext);

    // Verify
    assertEquals(1, sensorContext.allIssues().size());

    Issue issueRaised = sensorContext.allIssues().iterator().next();
    assertEquals(RuleKey.of("text", "MultiFileIfOneStringExistsThenBothMustExistCheck"), issueRaised.ruleKey());
  }

  @Test
  public void recordMatchTest(){
    // Set up
    MultiFileIfOneStringExistsThenBothMustExistCheck chk = new MultiFileIfOneStringExistsThenBothMustExistCheck();
    Map<InputFile, List<CrossFileScanPrelimIssue>> rawResults = new HashMap<>();
    chk.setCrossFileChecksRawResults(rawResults);

    // Execute
    DefaultInputFile inputFile1 = FileTestUtils.createInputFileShell(tempInputFilesPath + "somepath");
    DefaultInputFile inputFile2 = FileTestUtils.createInputFileShell(tempInputFilesPath + "somepath2");

    chk.setRuleKey(RuleKey.of("text","rule1"));
    chk.setTextSourceFile(new TextSourceFile(inputFile1));
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");

    chk.setRuleKey(RuleKey.of("text","rule2"));
    chk.setTextSourceFile(new TextSourceFile(inputFile2));
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");

    // Verify
    Assert.assertTrue(rawResults.size() == 2);
    List<CrossFileScanPrelimIssue> issuesForOneFile = rawResults.get(inputFile1);
    Assert.assertTrue(issuesForOneFile.size() == 2);

    issuesForOneFile = rawResults.get(inputFile2);
    Assert.assertTrue(issuesForOneFile.size() == 2);
  }

  @Test
  public void raiseIssuesAfterScanTest() {
    // Set up
    MultiFileIfOneStringExistsThenBothMustExistCheck chk = new MultiFileIfOneStringExistsThenBothMustExistCheck();
    Map<InputFile, List<CrossFileScanPrelimIssue>> rawResults = new HashMap<>();

    chk.setCrossFileChecksRawResults(rawResults);
    List<CrossFileScanPrelimIssue> issuesForOneFile = new LinkedList<>();
    // Case A: trigger and "must exist" are present. No issue should be raised.
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.TriggerPattern, RuleKey.of("text","rule1"), 1, "msg"));
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.MustAlsoExistPattern, RuleKey.of("text","rule1"), 1, "msg"));
    // Case B: trigger exists but "must exist" pattern is not present. An issue should be raised.
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.TriggerPattern, RuleKey.of("text","rule2"), 1, "msg"));  // no corresponding "must also exist" pattern was found, should trigger raising an issue
    // Case C: Trigger was not found. The "must exist" pattern is present. No issue should be raised.
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.MustAlsoExistPattern, RuleKey.of("text","rule3"), 1, "msg"));
    // Case D: Neither Trigger nor "must exist" was found. No issue should be raised.
       // This space intentionally left blank. No matches found.

    rawResults.put(FileTestUtils.createInputFileShell(tempInputFilesPath + "file1"), issuesForOneFile);

    // Execute
    chk.setRuleKey(RuleKey.of("text","rule1"));
    List<TextSourceFile> rule1Results = chk.raiseIssuesAfterScan();

    chk.setRuleKey(RuleKey.of("text","rule2"));
    List<TextSourceFile> rule2Results = chk.raiseIssuesAfterScan();

    chk.setRuleKey(RuleKey.of("text","rule3"));
    List<TextSourceFile> rule3Results = chk.raiseIssuesAfterScan();

    chk.setRuleKey(RuleKey.of("text","rule4"));
    List<TextSourceFile> rule4Results = chk.raiseIssuesAfterScan();

    // Verify
    Assert.assertTrue(rule1Results.size() == 0);
    Assert.assertTrue(rule2Results.size() == 1);
    Assert.assertTrue(rule3Results.size() == 0);
    Assert.assertTrue(rule4Results.size() == 0);

  }

  @Before
  public void createIssueSensorBackedByMocks() {
    Checks<Object> checks = mock(Checks.class);
    CheckFactory checkFactory = mock(CheckFactory.class);
    when(checkFactory.create(Mockito.anyString())).thenReturn(checks);
    when(checks.addAnnotatedChecks(Mockito.any(Iterable.class))).thenReturn(checks);

    List<Object> checksList = Arrays.asList(new Object[] {realIfOneStringExistsBothMustExistMultiFileCheck});
    when(checks.all()).thenReturn(checksList);

    when(checks.ruleKey(Mockito.isA(MultiFileIfOneStringExistsThenBothMustExistCheck.class))).thenReturn(RuleKey.of("text", "MultiFileIfOneStringExistsThenBothMustExistCheck"));

    sensor = new TextIssueSensor(fs, sensorContext, checkFactory);
  }

}
