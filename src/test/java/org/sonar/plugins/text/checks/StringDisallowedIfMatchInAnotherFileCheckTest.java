package org.sonar.plugins.text.checks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
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
import org.mockito.Mockito;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.text.TextLanguage;
import org.sonar.plugins.text.batch.TextIssueSensor;
import org.sonar.plugins.text.checks.AbstractCrossFileCheck.RulePart;

public class StringDisallowedIfMatchInAnotherFileCheckTest extends AbstractCrossFileCheckTester {

  private Project project;
  private DefaultFileSystem fs;
  private TextIssueSensor sensor;
  private final StringDisallowedIfMatchInAnotherFileCheck realStringDisallowedMultiFileCheck = new StringDisallowedIfMatchInAnotherFileCheck();
  private Issuable mockIssuable;

  @Before
  public void setUp() throws Exception {
    project = new Project("com.mycorp.projectA.service:service-do-X");
    fs = new DefaultFileSystem();
    fs.setBaseDir(new File("tmp/"));
  }

  // Code originally borrowed from IssueSensorTest...
  // This is a bit of an integration test as it wires up the TextIssueSensor. This enables us to know that these classes play nice together.
  @Test
  public void analyse_multi_class_integration_test() throws IOException {
    // Setup
    SensorContext sensorContext = mock(SensorContext.class);

      // Create files to be scanned
      // File containing trigger pattern
      DefaultInputFile inputFile = createInputFile("effective-pom.xml", TextLanguage.KEY);
      fs.add(inputFile);
      List<String> lines = Arrays.asList("The first line", "<target>1.8</target>", "The third line");
      Path file = Paths.get(inputFile.file().getAbsolutePath());
      inputFile.file().getParentFile().mkdirs();
      Files.write(file, lines, Charset.forName("UTF-8"));

      // File with disallowed config
      inputFile = createInputFile("feature-setup-env.properties", TextLanguage.KEY);
      fs.add(inputFile);
      lines = Arrays.asList("The first line", "JAVA_HOME=/software/java64/jdk1.7.0_60", "The third line");
      file = Paths.get(inputFile.file().getAbsolutePath());
      inputFile.file().getParentFile().mkdirs();
      Files.write(file, lines, Charset.forName("UTF-8"));

      // Configure the check
      realStringDisallowedMultiFileCheck.setTriggerFilePattern("**/effective-pom.xml");
      realStringDisallowedMultiFileCheck.setTriggerExpression(".*<target>1.8</target>.*");
      realStringDisallowedMultiFileCheck.setDisallowFilePattern("**/*setup-env*");
      realStringDisallowedMultiFileCheck.setDisallowExpression(".*JAVA_HOME=.*jdk1.(6|7).*");
      realStringDisallowedMultiFileCheck.setApplyExpressionToOneLineOfTextAtATime(true);

      realStringDisallowedMultiFileCheck.setMessage("Project compiled to target Java 8 is being booted under a prior JVM version.");

    // Run
    sensor.analyse(project, sensorContext);

    // Verify
    verify(mockIssuable).addIssue(Mockito.isA(Issue.class));
  }

  @Test
  public void analyse_multi_class_integration_test_multiline_aka_DOTALL_regex() throws IOException {
    // Setup
    SensorContext sensorContext = mock(SensorContext.class);

      // Create files to be scanned
      // File containing trigger pattern
      DefaultInputFile inputFile = createInputFile("effective-pom.xml", TextLanguage.KEY);
      fs.add(inputFile);
      List<String> lines = Arrays.asList("The first line", "<target>1.8</target>", "The third line");
      Path file = Paths.get(inputFile.file().getAbsolutePath());
      inputFile.file().getParentFile().mkdirs();
      Files.write(file, lines, Charset.forName("UTF-8"));

      // File with disallowed config
      inputFile = createInputFile("feature-setup-env.properties", TextLanguage.KEY);
      fs.add(inputFile);
      lines = Arrays.asList("The first line", "JAVA_HOME=/software/java64/jdk1.7.0_60", "The third line");
      file = Paths.get(inputFile.file().getAbsolutePath());
      inputFile.file().getParentFile().mkdirs();
      Files.write(file, lines, Charset.forName("UTF-8"));

      // Configure the check
      realStringDisallowedMultiFileCheck.setTriggerFilePattern("**/effective-pom.xml");
      realStringDisallowedMultiFileCheck.setTriggerExpression("(?s).*<target>1.8</target>.*third");
      realStringDisallowedMultiFileCheck.setDisallowFilePattern("**/*setup-env*");
      realStringDisallowedMultiFileCheck.setDisallowExpression("(?s).*JAVA_HOME=.*jdk1.(6|7).*third");
      realStringDisallowedMultiFileCheck.setApplyExpressionToOneLineOfTextAtATime(false);

      realStringDisallowedMultiFileCheck.setMessage("Project compiled to target Java 8 is being booted under a prior JVM version.");

    // Run
    sensor.analyse(project, sensorContext);

    // Verify
    verify(mockIssuable).addIssue(Mockito.isA(Issue.class));
  }

  @Test
  public void recordMatchTest(){
    // Set up
    StringDisallowedIfMatchInAnotherFileCheck chk = new StringDisallowedIfMatchInAnotherFileCheck();
    Map<InputFile, List<CrossFileScanPrelimIssue>> rawResults = new HashMap<InputFile, List<CrossFileScanPrelimIssue>>();
    chk.setCrossFileChecksRawResults(rawResults);

    // Execute
    chk.setRuleKey(RuleKey.of("text","rule1"));
    chk.setTextSourceFile(new TextSourceFile(new DefaultInputFile("somepath")));
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");

    chk.setRuleKey(RuleKey.of("text","rule2"));
    chk.setTextSourceFile(new TextSourceFile(new DefaultInputFile("someOtherPath")));
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");
    chk.recordMatch(RulePart.TriggerPattern, 1, "msg");

    // Verify
    Assert.assertTrue(rawResults.size() == 2);
    //List<CrossFileScanPrelimIssue> issuesForOneFile = rawResults.values().iterator().next();
    List<CrossFileScanPrelimIssue> issuesForOneFile = rawResults.get(new DefaultInputFile("somepath"));
    Assert.assertTrue(issuesForOneFile.size() == 2);

    issuesForOneFile = rawResults.get(new DefaultInputFile("someOtherPath"));
    Assert.assertTrue(issuesForOneFile.size() == 2);
  }

  @Test
  public void raiseIssuesAfterScanTest() {
    // Set up
    StringDisallowedIfMatchInAnotherFileCheck chk = new StringDisallowedIfMatchInAnotherFileCheck();
    Map<InputFile, List<CrossFileScanPrelimIssue>> rawResults = new HashMap<InputFile, List<CrossFileScanPrelimIssue>>();

    chk.setCrossFileChecksRawResults(rawResults);
    List<CrossFileScanPrelimIssue> issuesForOneFile = new LinkedList<CrossFileScanPrelimIssue>();
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.TriggerPattern, RuleKey.of("text","rule1"), 1, "msg"));
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.DisallowPattern, RuleKey.of("text","rule1"), 1, "msg"));
    issuesForOneFile.add(new CrossFileScanPrelimIssue(RulePart.DisallowPattern, RuleKey.of("text","rule2"), 1, "msg"));  // not triggered, should not raise an issue
    rawResults.put(new DefaultInputFile("file1"), issuesForOneFile);

    // Execute
    chk.setRuleKey(RuleKey.of("text","rule1"));
    List<TextSourceFile> rule1Results = chk.raiseIssuesAfterScan();

    chk.setRuleKey(RuleKey.of("text","rule2"));
    List<TextSourceFile> rule2Results = chk.raiseIssuesAfterScan();

    // Verify
    Assert.assertTrue(rule1Results.size() == 1);
    Assert.assertTrue(rule2Results.size() == 0);

  }

  @Before
  public void createIssueSensorBackedByMocks() {
    ResourcePerspectives resourcePerspectives = mock(ResourcePerspectives.class);
    Checks<Object> checks = mock(Checks.class);
    CheckFactory checkFactory = mock(CheckFactory.class);
    when(checkFactory.create(Mockito.anyString())).thenReturn(checks);
    List<Object> checksList = Arrays.asList(new Object[] {realStringDisallowedMultiFileCheck});
    when(checks.all()).thenReturn(checksList);

    when(checks.ruleKey(Mockito.isA(StringDisallowedIfMatchInAnotherFileCheck.class))).thenReturn(RuleKey.of("text", "StringDisallowedIfMatchInAnotherFileCheck"));
//    realStringDisallowedMultiFileCheck.setRuleKey(RuleKey.parse("text:StringDisallowedIfMatchInAnotherFileCheck")); // Not strictly necessary here. Normally set by the framework to the value in the Check class's annotation

    when(checks.addAnnotatedChecks(Mockito.anyCollection())).thenReturn(checks);
    mockIssuable = mock(Issuable.class);
    when(resourcePerspectives.as(Mockito.eq(Issuable.class), Mockito.isA(InputFile.class))).thenReturn(mockIssuable);
    IssueBuilder mockIssueBuilder = mock(IssueBuilder.class);
    when(mockIssuable.newIssueBuilder()).thenReturn(mockIssueBuilder);
    when(mockIssueBuilder.ruleKey(Mockito.isA(RuleKey.class))).thenReturn(mockIssueBuilder);
    when(mockIssueBuilder.line(Mockito.anyInt())).thenReturn(mockIssueBuilder);
    when(mockIssueBuilder.message(Mockito.anyString())).thenReturn(mockIssueBuilder);
    when(mockIssueBuilder.build()).thenReturn(mock(Issue.class));

    sensor = new TextIssueSensor(fs, resourcePerspectives, checkFactory, project);
  }

  private DefaultInputFile createInputFile(final String name, final String language) {
    return new DefaultInputFile(name)
      .setLanguage(language)
      .setType(InputFile.Type.MAIN)
      .setAbsolutePath(new File("src/test/resources/sensorIT/StringDisallowedIfMatchInAnotherFile/" + name).getAbsolutePath());
  }

}
