package org.sonar.plugins.text.testutils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.Metadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.notifications.AnalysisWarnings;

public class FileTestUtils {
  private FileTestUtils() {}

  private static final String language = "text";
  private static final String moduleKey = "."; // Choice: Not using this extra subdirectory in paths for our unit tests

  public static DefaultInputFile createInputFile(final Path relativePathToFile, final String fileContent) {
    try {
      FileUtils.write(relativePathToFile.toFile(), fileContent, StandardCharsets.UTF_8);

      return createInputFile(relativePathToFile.toString());

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Use when a file exists. File metadata used by the highlighting features will be available.
   */
  public static DefaultInputFile createInputFile(final String relativePathToFile) {
    try {
      DefaultInputFile inputFile = createInputFileShell(relativePathToFile);

      Metadata metadata = new FileMetadata(noOpAnalysisWarnings).readMetadata(new FileInputStream(inputFile.file()), inputFile.charset(), inputFile.absolutePath());
      return inputFile.setMetadata(metadata);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Use when you don't actually have any file content and don't need any. Note that file metadata used for highlighting won't be available.
   *
   */
  public static DefaultInputFile createInputFileShell(final String relativePathToFile) {
      return TestInputFileBuilder.create(moduleKey, relativePathToFile)
        .setLanguage(language)
        .setType(InputFile.Type.MAIN)
        .setCharset(StandardCharsets.UTF_8)
        .build();
  }

  private static AnalysisWarnings noOpAnalysisWarnings
    = new AnalysisWarnings() {
        @Override public void addUnique(String text) {}
      };

}
