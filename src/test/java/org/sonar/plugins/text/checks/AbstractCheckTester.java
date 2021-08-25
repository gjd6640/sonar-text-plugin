package org.sonar.plugins.text.checks;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractCheckTester {

  private static final Path tempfilesLocation = Paths.get("target", "surefire-test-resources");

  @BeforeClass
  public static void init() throws IOException {
    Files.createDirectories(tempfilesLocation);
  }

  @org.junit.Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder(tempfilesLocation.toFile());

  protected static final String INCORRECT_NUMBER_OF_VIOLATIONS = "Incorrect number of violations";

  protected TextSourceFile parseAndCheck(final File file, final AbstractTextCheck check, final String projectKey) {
    TextSourceFile textSourceFile = new TextSourceFile(new TestInputFileBuilder(".", file.getPath()).build());

    check.validate(textSourceFile, projectKey);
    return textSourceFile;
  }

  protected DefaultFileSystem createFileSystem() throws IOException {
    File workDir = temporaryFolder.getRoot();
    Files.createDirectories(workDir.toPath());
    DefaultFileSystem fs = new DefaultFileSystem(workDir);
    fs.setEncoding(Charset.defaultCharset());

    return fs;
  }

  protected File createTempFile(final String content) throws IOException {
    File f = temporaryFolder.newFile("file.xml");
    FileUtils.write(f, content, StandardCharsets.UTF_8);

    return f;
  }

}
