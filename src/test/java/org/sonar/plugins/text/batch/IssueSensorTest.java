package org.sonar.plugins.text.batch;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.rule.CheckFactory;
import org.sonar.api.batch.rule.Checks;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.issue.Issuable.IssueBuilder;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.text.TextLanguage;
import org.sonar.plugins.text.checks.AbstractTextCheck;
import org.sonar.plugins.text.checks.TextIssue;
import org.sonar.plugins.text.checks.TextSourceFile;

public class IssueSensorTest {

	  private Project project;
	  private DefaultFileSystem fs;
	  private IssueSensor sensor; 
	  private AbstractTextCheck textCheckMock;
	  private Issuable mockIssuable;

	  @Before
	  public void setUp() throws Exception {
	    project = new Project("");
	    fs = new DefaultFileSystem();
	    fs.setBaseDir(new File("tmp/"));
	  }

	  @Test
	  public void shouldExecuteOnProject_No_EmptyProject() {
		// No setup needed. Project starts out empty already 
		assertThat(sensor.shouldExecuteOnProject(project)).isFalse();
	  }

	  @Test
	  public void shouldExecuteOnProject_No_OnlyJavaFiles() {
 	    fs.add(createInputFile("file.java", "java"));
 	    assertThat(sensor.shouldExecuteOnProject(project)).isFalse(); 
	  }

	  @Test
	  public void shouldExecuteOnProject_Yes_ProjectHasTextFile() {
	    fs.add(createInputFile("file.txt", TextLanguage.KEY));
	    assertThat(sensor.shouldExecuteOnProject(project)).isTrue();
	  }

	  @Test
	  public void analyse() {
		// Setup
		SensorContext sensorContext = mock(SensorContext.class);
	
		// Run
		fs.add(createInputFile("setup.properties", TextLanguage.KEY));

		Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				TextSourceFile sourceFile = (TextSourceFile)invocation.getArguments()[0];
				sourceFile.addViolation(new TextIssue(mock(RuleKey.class), 1, "rule violated"));
				return null;
			}
		}).when(textCheckMock).validate(Mockito.any(TextSourceFile.class));

	    sensor.analyse(project, sensorContext);

	    verify(mockIssuable).addIssue(Mockito.isA(Issue.class));
	  }

	  @Before
	  public void createIssueSensorBackedByMocks() {
			ResourcePerspectives resourcePerspectives = mock(ResourcePerspectives.class);
			Checks<Object> checks = mock(Checks.class);
			CheckFactory checkFactory = mock(CheckFactory.class);
			when(checkFactory.create(Mockito.anyString())).thenReturn(checks);
			textCheckMock = mock(AbstractTextCheck.class);
			List<Object> checksList = Arrays.asList(new Object[] {textCheckMock});
			when(checks.all()).thenReturn(checksList);
			
			when(checks.addAnnotatedChecks(Mockito.anyCollection())).thenReturn(checks);
			mockIssuable = mock(Issuable.class); 
			when(resourcePerspectives.as(Mockito.eq(Issuable.class), Mockito.isA(InputFile.class))).thenReturn(mockIssuable);
			IssueBuilder mockIssueBuilder = mock(IssueBuilder.class);
			when(mockIssuable.newIssueBuilder()).thenReturn(mockIssueBuilder);
			when(mockIssueBuilder.ruleKey(Mockito.isA(RuleKey.class))).thenReturn(mockIssueBuilder);
			when(mockIssueBuilder.line(Mockito.anyInt())).thenReturn(mockIssueBuilder);
			when(mockIssueBuilder.message(Mockito.anyString())).thenReturn(mockIssueBuilder);
			when(mockIssueBuilder.build()).thenReturn(mock(Issue.class));

			sensor = new IssueSensor(fs, resourcePerspectives, checkFactory);
	  }
	  
	  private DefaultInputFile createInputFile(String name, String language) {
		    return new DefaultInputFile(name)
		      .setLanguage(language)
		      .setType(InputFile.Type.MAIN)
		      .setAbsolutePath(new File("src/test/resources/parsers/linecount/" + name).getAbsolutePath());
		  }

}
