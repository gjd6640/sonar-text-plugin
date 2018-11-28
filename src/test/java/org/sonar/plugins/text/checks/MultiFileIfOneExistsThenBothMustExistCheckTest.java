package org.sonar.plugins.text.checks;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
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
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.text.TextLanguage;
import org.sonar.plugins.text.batch.TextIssueSensor;
import org.sonar.plugins.text.checks.AbstractCrossFileCheck.RulePart;
import org.sonar.plugins.text.utils.InputFileUtil;

public class MultiFileIfOneExistsThenBothMustExistCheckTest extends AbstractCrossFileCheckTester {

  private TextIssueSensor sensor;
  private final MultiFileIfOneStringExistsThenBothMustExistCheck realIfOneStringExistsBothMustExistMultiFileCheck = new MultiFileIfOneStringExistsThenBothMustExistCheck();

  // Code originally borrowed from IssueSensorTest...
  // This is a bit of an integration test as it wires up the TextIssueSensor. This enables us to know that these classes play nice together.
  @Test
  public void analyse_multi_class_integration_test() throws IOException {
      // Setup
      DefaultFileSystem fs = super.createFileSystem();
      getSensorContextTester().setFileSystem(fs);

      // Create files to be scanned
      // File containing trigger pattern
      File tempFile1 = super.createTempFile("effective-pom.xml", "");
      DefaultInputFile inputFile = InputFileUtil.createInputFile(tempFile1.getPath(), TextLanguage.LANGUAGE_KEY);
      fs.add(inputFile);
      List<String> lines = Arrays.asList("The first line", "<target>1.8</target>", "The third line");
      Path file = Paths.get(inputFile.file().getAbsolutePath());
      inputFile.file().getParentFile().mkdirs();
      Files.write(file, lines, Charset.forName("UTF-8"));



      // File with disallowed config
      tempFile1 = super.createTempFile("feature-setup-env.properties", "");
      inputFile = InputFileUtil.createInputFile(tempFile1.getPath(), TextLanguage.LANGUAGE_KEY);
      fs.add(inputFile);
      lines = Arrays.asList("The first line", "JAVA_HOME=/software/java64/jdk1.7.0_60", "The third line");
      file = Paths.get(inputFile.file().getAbsolutePath());
      inputFile.file().getParentFile().mkdirs();
      Files.write(file, lines, Charset.forName("UTF-8"));

      // Configure the check
      realIfOneStringExistsBothMustExistMultiFileCheck.setTriggerFilePattern("**/effective-pom.xml");
      realIfOneStringExistsBothMustExistMultiFileCheck.setTriggerExpression(".*<target>1.8</target>.*");
      realIfOneStringExistsBothMustExistMultiFileCheck.setMustAlsoExistFilePattern("**/*setup-env*");
      realIfOneStringExistsBothMustExistMultiFileCheck.setMustAlsoExistExpression(".*-DFooProperty");
      realIfOneStringExistsBothMustExistMultiFileCheck.setApplyExpressionToOneLineOfTextAtATime(true);

      realIfOneStringExistsBothMustExistMultiFileCheck.setMessage("Project compiled to target Java 8 doesn't have recommended system property 'FooProperty'.");

    // Run
    sensor.execute(getSensorContextTester());

    // Verify
    assertTrue(getSensorContextTester().allIssues().size() == 1);
  }

  @Test
  public void analyse_multi_class_integration_test_multiline_aka_DOTALL_regex() throws IOException {
      // Setup
      DefaultFileSystem fs = super.createFileSystem("tmp2");
      getSensorContextTester().setFileSystem(fs);

      // Create files to be scanned
      // File containing trigger pattern
      File tempFile1 = super.createTempFile("effective-pom.xml", "");

      DefaultInputFile inputFile = InputFileUtil.createInputFile(tempFile1.getPath(), TextLanguage.LANGUAGE_KEY);
      fs.add(inputFile);
      List<String> lines = Arrays.asList("The first line", "<target>1.8</target>", "The third line");
      Path file = Paths.get(inputFile.file().getAbsolutePath());
      inputFile.file().getParentFile().mkdirs();
      Files.write(file, lines, Charset.forName("UTF-8"));

      // File with disallowed config
      tempFile1 = super.createTempFile("feature-setup-env.properties", "");
      inputFile = InputFileUtil.createInputFile(tempFile1.getPath(), TextLanguage.LANGUAGE_KEY);
      fs.add(inputFile);
      lines = Arrays.asList("The first line", "JAVA_HOME=/software/java64/jdk1.7.0_60", "The third line");
      file = Paths.get(inputFile.file().getAbsolutePath());
      inputFile.file().getParentFile().mkdirs();
      Files.write(file, lines, Charset.forName("UTF-8"));

      // Configure the check
      realIfOneStringExistsBothMustExistMultiFileCheck.setTriggerFilePattern("**/effective-pom.xml");
      realIfOneStringExistsBothMustExistMultiFileCheck.setTriggerExpression("(?s).*<target>1.8</target>.*third");
      realIfOneStringExistsBothMustExistMultiFileCheck.setMustAlsoExistFilePattern("**/*setup-env*");
      realIfOneStringExistsBothMustExistMultiFileCheck.setMustAlsoExistExpression("(?s).*-DFooProperty.*third");
      realIfOneStringExistsBothMustExistMultiFileCheck.setApplyExpressionToOneLineOfTextAtATime(false);

      realIfOneStringExistsBothMustExistMultiFileCheck.setMessage("Project compiled to target Java 8 is being booted under a prior JVM version.");

      // Run
///      SensorContextTester localSensorContextTester = SensorContextTester.create(Paths.get("tmp2/"));
      sensor.execute(getSensorContextTester());

      // Verify
      assertTrue(getSensorContextTester().allIssues().size() == 1);
  }

  @Test
  public void recordMatchTest(){
    // Set up
    MultiFileIfOneStringExistsThenBothMustExistCheck chk = new MultiFileIfOneStringExistsThenBothMustExistCheck();
    Map<InputFile, List<CrossFileScanPrelimIssue>> rawResults = new HashMap<InputFile, List<CrossFileScanPrelimIssue>>();
    chk.setCrossFileChecksRawResults(rawResults);

    // Execute
    chk.setRuleKey(RuleKey.of("text","rule1"));
    chk.setTextSourceFile(new TextSourceFile(InputFileUtil.createInputFile("somepath")));
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");

    chk.setRuleKey(RuleKey.of("text","rule2"));
    chk.setTextSourceFile(new TextSourceFile(InputFileUtil.createInputFile("someOtherPath")));
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");

    // Verify
    Assert.assertTrue(rawResults.size() == 2);
    List<CrossFileScanPrelimIssue> issuesForOneFile = rawResults.get(InputFileUtil.createInputFile("somepath"));
    Assert.assertTrue(issuesForOneFile.size() == 2);

    issuesForOneFile = rawResults.get(InputFileUtil.createInputFile("someOtherPath"));
    Assert.assertTrue(issuesForOneFile.size() == 2);
  }

  @Test
  public void raiseIssuesAfterScanTest() {
    // Set up
    MultiFileIfOneStringExistsThenBothMustExistCheck chk = new MultiFileIfOneStringExistsThenBothMustExistCheck();
    Map<InputFile, List<CrossFileScanPrelimIssue>> rawResults = new HashMap<InputFile, List<CrossFileScanPrelimIssue>>();

    chk.setCrossFileChecksRawResults(rawResults);
    List<CrossFileScanPrelimIssue> issuesForOneFile = new LinkedList<CrossFileScanPrelimIssue>();
    // Case A: trigger and "must exist" are present. No issue should be raised.
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.TriggerPattern, RuleKey.of("text","rule1"), 1, "msg"));
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.MustAlsoExistPattern, RuleKey.of("text","rule1"), 1, "msg"));
    // Case B: trigger exists but "must exist" pattern is not present. An issue should be raised.
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.TriggerPattern, RuleKey.of("text","rule2"), 1, "msg"));  // no corresponding "must also exist" pattern was found, should trigger raising an issue
    // Case C: Trigger was not found. The "must exist" pattern is present. No issue should be raised.
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.MustAlsoExistPattern, RuleKey.of("text","rule3"), 1, "msg"));
    // Case D: Neither Trigger nor "must exist" was found. No issue should be raised.
       // This space intentionally left blank. No matches found.

    rawResults.put(InputFileUtil.createInputFile("file1"), issuesForOneFile);

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
  public void createIssueSensorBackedByMocks() throws IOException {
    Checks<Object> checks = mock(Checks.class);
    CheckFactory checkFactory = mock(CheckFactory.class);
    when(checkFactory.create(Matchers.anyString())).thenReturn(checks);
    List<Object> checksList = Arrays.asList(new Object[] {realIfOneStringExistsBothMustExistMultiFileCheck});
    when(checks.all()).thenReturn(checksList);

    when(checks.ruleKey(Matchers.isA(StringDisallowedIfMatchInAnotherFileCheck.class))).thenReturn(RuleKey.of("text", "StringDisallowedIfMatchInAnotherFileCheck"));
    when(checks.ruleKey(Matchers.isA(MultiFileIfOneStringExistsThenBothMustExistCheck.class))).thenReturn(RuleKey.of("text", "MultiFileIfOneStringExistsThenBothMustExistCheck"));
//    realStringDisallowedMultiFileCheck.setRuleKey(RuleKey.parse("text:StringDisallowedIfMatchInAnotherFileCheck")); // Not strictly necessary here. Normally set by the framework to the value in the Check class's annotation

    when(checks.addAnnotatedChecks(Matchers.anyCollection())).thenReturn(checks);
    File workarea = temporaryFolder.newFolder("MFIOETBMECT");
    DefaultInputModule project = new DefaultInputModule(ProjectDefinition.create().setKey("com.mycorp.projectA.service:service-do-X").setBaseDir(workarea).setWorkDir(workarea));
    sensor = new TextIssueSensor(checkFactory, project);
  }
}
