package org.sonar.plugins.text.checks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

public abstract class AbstractCheckTester {

  @org.junit.Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  protected static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  private SensorContextTester sensorContextTester = SensorContextTester.create(Paths.get("."));
  protected SensorContextTester getSensorContextTester() {
    return sensorContextTester;
  }
  protected TextSourceFile parseAndCheck(final File file, final AbstractTextCheck check, final String projectKey) {
    InputFile inputFile = new TestInputFileBuilder("blahModuleKey", file.getPath())
        .setModuleBaseDir(file.toPath().getParent())
        .setType(InputFile.Type.MAIN)
        .build();  

    TextSourceFile textSourceFile = new TextSourceFile(inputFile);
    check.validate(textSourceFile, projectKey);
    return textSourceFile;
  }

  protected DefaultFileSystem createFileSystem() {
    /*
    File workDir;
	try {
		workDir = temporaryFolder.newFolder("temp");
	} catch (IOException e) {
		throw new RuntimeException(e);
	}

    DefaultFileSystem fs = new DefaultFileSystem(workDir);
    fs.setEncoding(Charset.defaultCharset());
    
    return fs;*/
    DefaultFileSystem fs = sensorContextTester.fileSystem();
    fs.setWorkDir(temporaryFolder.getRoot().toPath());
    return fs;
  }

  protected File createTempFile(final String content) throws IOException {
    
    File f = temporaryFolder.newFile("file.xml");
    FileUtils.write(f, content);

    return f;
  }

}
