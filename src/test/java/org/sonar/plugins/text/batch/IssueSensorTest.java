package org.sonar.plugins.text.batch;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.MalformedInputException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputModule;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.text.TextLanguage;
import org.sonar.plugins.text.checks.AbstractTextCheck;
import org.sonar.plugins.text.checks.TextIssue;
import org.sonar.plugins.text.checks.TextSourceFile;
import org.sonar.plugins.text.utils.InputFileUtil;

public class IssueSensorTest {

	  private InputModule project;
	  private TextIssueSensor sensor;
	  SensorContextTester sensorContextTester;
	  private AbstractTextCheck textCheckMock;

	  @org.junit.Rule // Created once for each test!
	  public TemporaryFolder temporaryFolder = new TemporaryFolder(Paths.get("./target/").toAbsolutePath().toFile());

	  @Before
	  public void setUp() throws Exception {

	    File workarea = temporaryFolder.newFolder("IssueSensorTest");
	    project = new DefaultInputModule(ProjectDefinition.create().setKey("com.mycorp.projectA.service:service-do-X").setBaseDir(workarea).setWorkDir(workarea));
	    sensorContextTester = SensorContextTester.create(temporaryFolder.getRoot().toPath());
	    fs = sensorContextTester.fileSystem();

//	    project = new DefaultInputModule(ProjectDefinition.create().setKey("com.mycorp.projectA.service:service-do-X"));
	  }
	  DefaultFileSystem fs;
	  @Test
	  public void analyse() {
		// Setup

		// Run
		fs.add(InputFileUtil.createInputFile("setup.properties", TextLanguage.LANGUAGE_KEY));

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(final InvocationOnMock invocation) throws Throwable {
				TextSourceFile sourceFile = (TextSourceFile)invocation.getArguments()[0];
				sourceFile.addViolation(new TextIssue(mock(RuleKey.class), 1, "rule violated"));
				return null;
			}
		}).when(textCheckMock).validate(Matchers.any(TextSourceFile.class), Matchers.matches("com.mycorp.projectA.service:service-do-X"));

	    sensor.execute(sensorContextTester);

	    assertTrue(sensorContextTester.allIssues().size() == 1);

	    //verify(mockIssuable).addIssue(Mockito.isA(Issue.class));
	  }

    @Test  // Covers any failure case including where the scanner encounters bytes that don't match the current character set
	  public void analyse_ProceedQuietlyWhenSensorThrowsException() {
	    // Setup
//      SensorContextTester localSensorContextTester = SensorContextTester.create(Paths.get("./"));
//      DefaultFileSystem fs = localSensorContextTester.fileSystem();

	    // One of these will NOT be scanned due to an exception being encountered
	    fs.add(InputFileUtil.createInputFile("setup.properties", TextLanguage.LANGUAGE_KEY));
      fs.add(InputFileUtil.createInputFile("setup.properties2", TextLanguage.LANGUAGE_KEY));

	    final AtomicBoolean firstCall = new AtomicBoolean(true);

	    Mockito.doAnswer(new Answer<Void>() {
	      @Override
	      public Void answer(final InvocationOnMock invocation) throws Throwable {
	        if (firstCall.get()) {
	          firstCall.set(false);
	          throw new RuntimeException(new MalformedInputException(1));
	        } else {
            TextSourceFile sourceFile = (TextSourceFile)invocation.getArguments()[0];
            sourceFile.addViolation(new TextIssue(mock(RuleKey.class), 1, "rule violated"));
            return null;
	        }
	      }
	    }).when(textCheckMock).validate(Matchers.any(TextSourceFile.class), Matchers.matches("com.mycorp.projectA.service:service-do-X"));

	    when(textCheckMock.getRuleKey()).thenReturn(RuleKey.parse("SomeLanguage:RuleKey"));

	    // Run
	    sensor.execute(sensorContextTester);

	    // Assertions
	    assertTrue(sensorContextTester.allIssues().size() == 1);
	    //verify(mockIssuable).addIssue(Mockito.isA(Issue.class));
   }

	  @Before
	  public void createIssueSensorBackedByMocks() {
			ResourcePerspectives resourcePerspectives = mock(ResourcePerspectives.class);
			Checks<Object> checks = mock(Checks.class);
			CheckFactory checkFactory = mock(CheckFactory.class);
			when(checkFactory.create(Matchers.anyString())).thenReturn(checks);
			textCheckMock = mock(AbstractTextCheck.class);
			List<Object> checksList = Arrays.asList(new Object[] {textCheckMock});
			when(checks.all()).thenReturn(checksList);
			when(checks.addAnnotatedChecks(Matchers.anyCollection())).thenReturn(checks);
			/*
			mockIssuable = mock(Issuable.class);
			when(resourcePerspectives.as(Mockito.eq(Issuable.class), Mockito.isA(InputFile.class))).thenReturn(mockIssuable);
			IssueBuilder mockIssueBuilder = mock(IssueBuilder.class);
			when(mockIssuable.newIssueBuilder()).thenReturn(mockIssueBuilder);

			when(mockIssueBuilder.ruleKey(Mockito.isA(RuleKey.class))).thenReturn(mockIssueBuilder);
			when(mockIssueBuilder.line(Mockito.anyInt())).thenReturn(mockIssueBuilder);
			when(mockIssueBuilder.message(Mockito.anyString())).thenReturn(mockIssueBuilder);
			when(mockIssueBuilder.build()).thenReturn(mock(Issue.class));
*/
			sensor = new TextIssueSensor(checkFactory, project);
	  }
/*
	  private DefaultInputFile createInputFile(final String name, final String language) {
	    return new TestInputFileBuilder("blahModuleKey", name)
	         .setModuleBaseDir(Paths.get("."))
	         .setLanguage(language)
	         .setType(InputFile.Type.MAIN)
	         .build();
	  }
*/
}
