package org.sonar.plugins.text.checks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.DefaultInputFile;

public abstract class AbstractCheckTester {

  @org.junit.Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  protected static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  protected TextSourceFile parseAndCheck(final File file, final AbstractTextCheck check, final String projectKey) {
    TextSourceFile textSourceFile = new TextSourceFile(new DefaultInputFile(file.getPath()).setAbsolutePath(file.getAbsolutePath()));

    check.validate(textSourceFile, projectKey);
    return textSourceFile;
  }

  protected DefaultFileSystem createFileSystem() {
    File workDir;
	try {
		workDir = temporaryFolder.newFolder("temp");
	} catch (IOException e) {
		throw new RuntimeException(e);
	}

    DefaultFileSystem fs = new DefaultFileSystem();
    fs.setEncoding(Charset.defaultCharset());
    fs.setWorkDir(workDir);

    return fs;
  }

  protected File createTempFile(final String content) throws IOException {
    File f = temporaryFolder.newFile("file.xml");
    FileUtils.write(f, content);

    return f;
  }

}
