package org.sonar.plugins.text.batch;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.Metadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.internal.DefaultSensorDescriptor;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.text.TextLanguage;
import org.sonar.plugins.text.checks.AbstractTextCheck;
import org.sonar.plugins.text.checks.TextIssue;
import org.sonar.plugins.text.checks.TextSourceFile;
import org.sonar.plugins.text.testutils.FileTestUtils;

public class IssueSensorTest {

    private File tempFileSystemBaseDir = Paths.get("target", "surefire-test-resources","IssueSensorTest").toFile();
    private RuleKey dummyRuleKey = RuleKey.of("repoKey", "ruleKey");

    private SensorContextTester sensorContextTester;
	  private DefaultFileSystem fs;
	  private TextIssueSensor sensor;
	  private AbstractTextCheck textCheckMock;

	  @Before
	  public void setUp() throws Exception {
	    Files.createDirectories(tempFileSystemBaseDir.toPath());
	    fs = new DefaultFileSystem(tempFileSystemBaseDir);
	    sensorContextTester = SensorContextTester.create(tempFileSystemBaseDir);
	  }

	  @Test
	  public void test_descriptor() throws Exception {
	    DefaultSensorDescriptor sensorDescriptor = new DefaultSensorDescriptor();
	    sensor.describe(sensorDescriptor);
	    assertThat(sensorDescriptor.name()).isEqualTo("Inspects the project's files using custom rules built from regular expressions for known combinations of problematic configurations and/or code.");
	  }

	  @Test
	  public void analyse() throws IOException {
  		// Setup
	    Path sampleFilePath = Paths.get(tempFileSystemBaseDir.toString(), "setup.properties");
	    FileUtils.write(sampleFilePath.toFile(), "asdf\nasdf2\nasdf3", StandardCharsets.UTF_8);

      fs.add(FileTestUtils.createInputFile(sampleFilePath.toString()));

      Mockito.doAnswer(new Answer<Void>() {
        @Override
        public Void answer(final InvocationOnMock invocation) throws Throwable {
          TextSourceFile sourceFile = (TextSourceFile)invocation.getArguments()[0];
          sourceFile.addViolation(new TextIssue(dummyRuleKey, 1, "rule violated"));
          return null;
        }
      }).when(textCheckMock).validate(Mockito.any(TextSourceFile.class), Mockito.matches("projectKey"));

	    // Run
	    sensor.execute(sensorContextTester);

	    // Verify
	    assertEquals(1, sensorContextTester.allIssues().size());

	    Issue issueRaised = sensorContextTester.allIssues().iterator().next();
	    assertEquals(RuleKey.of("repoKey", "ruleKey"), issueRaised.ruleKey());
	    assertEquals(".:target/surefire-test-resources/IssueSensorTest/setup.properties", issueRaised.primaryLocation().inputComponent().key());
	    assertEquals("rule violated", issueRaised.primaryLocation().message());
	    assertEquals(1, issueRaised.primaryLocation().textRange().start().line());
	  }

    @Test  // Covers any failure case including where the scanner encounters bytes that don't match the current character set
	  public void analyse_ProceedQuietlyWhenSensorThrowsException() {
	    // Setup

	    // One of these will NOT be scanned due to an exception being encountered
	    fs.add(createInputFile("setup.properties", TextLanguage.LANGUAGE_KEY));
      fs.add(createInputFile("setup.properties2", TextLanguage.LANGUAGE_KEY));

	    final AtomicBoolean firstCall = new AtomicBoolean(true);

	    Mockito.doAnswer(new Answer<Void>() {
	      @Override
	      public Void answer(final InvocationOnMock invocation) throws Throwable {
	        if (firstCall.get()) {
	          firstCall.set(false);
	          throw new RuntimeException(new MalformedInputException(1));
	        } else {
            TextSourceFile sourceFile = (TextSourceFile)invocation.getArguments()[0];
            sourceFile.addViolation(new TextIssue(dummyRuleKey, 1, "rule violated"));
            return null;
	        }
	      }
	    }).when(textCheckMock).validate(Mockito.any(TextSourceFile.class), Mockito.matches("projectKey"));

	    // Run
	    sensor.execute(sensorContextTester);

	    // Assertions
	    assertEquals(1, sensorContextTester.allIssues().size());
   }

	  @Before
    public void createIssueSensorBackedByMocks() {
			CheckFactory checkFactory = mock(CheckFactory.class);
      Checks<Object> checks = mock(Checks.class);
			when(checkFactory.create(Mockito.anyString())).thenReturn(checks);
			when(checks.addAnnotatedChecks(Mockito.any(Iterable.class))).thenReturn(checks);
			textCheckMock = mock(AbstractTextCheck.class);
			List<Object> checksList = Arrays.asList(new Object[] {textCheckMock});
			when(checks.all()).thenReturn(checksList);

      sensor = new TextIssueSensor(fs, sensorContextTester, checkFactory);
    }

	  private DefaultInputFile createInputFile(final String name, final String language) {
	    return TestInputFileBuilder.create(".", Paths.get(tempFileSystemBaseDir.toPath().toString(), name).toString())
          .setLanguage(language)
          .setType(InputFile.Type.MAIN)
          .setMetadata(new Metadata(2, 2, "huh?", new int[] {0,10}, new int[] {9,19}, 19))
	        .build();
	  }

}
