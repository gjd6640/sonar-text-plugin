package org.sonar.plugins.text.utils;

import java.nio.file.Paths;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.Metadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

public class InputFileUtil {
  public static DefaultInputFile createInputFile(final String path) {
    return createInputFile(path, null);
  }

  public static int DEFAULT_FILE_LENGTH_METADATA_VALUE = 50;

  public static DefaultInputFile createInputFile(final String path, final String language) {
    TestInputFileBuilder fileBuilder = new TestInputFileBuilder("blahModuleKey", path)
    .setModuleBaseDir(Paths.get("."))
    .setLanguage(language)
    .setType(InputFile.Type.MAIN)
    .setLines(DEFAULT_FILE_LENGTH_METADATA_VALUE);

    if (language != null) {
      fileBuilder.setLanguage(language);
    }
    DefaultInputFile f = fileBuilder.build();
    f.setMetadata(new Metadata(DEFAULT_FILE_LENGTH_METADATA_VALUE,
                                DEFAULT_FILE_LENGTH_METADATA_VALUE,  // not carefully considered. feel free to improve...
                                "huh?", // not carefully considered. feel free to improve...
                                new int[DEFAULT_FILE_LENGTH_METADATA_VALUE], // not carefully considered. feel free to improve...
                                new int[DEFAULT_FILE_LENGTH_METADATA_VALUE], // not carefully considered. feel free to improve...
                                DEFAULT_FILE_LENGTH_METADATA_VALUE)); // not carefully considered. feel free to improve...
    return f;
  }
}
