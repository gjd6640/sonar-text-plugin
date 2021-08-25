package org.sonar.plugins.text.checks;

import java.io.File;

import org.sonar.api.batch.fs.internal.TestInputFileBuilder;

public abstract class AbstractCrossFileCheckTester extends AbstractCheckTester {

  @Override
  protected TextSourceFile parseAndCheck(final File file, final AbstractTextCheck check, final String projectKey) {
    if (check instanceof AbstractCrossFileCheck == false) {
      throw new IllegalArgumentException("This abstract class is only for use with cross-file checks");
    }
    TextSourceFile textSourceFile = new TextSourceFile(new TestInputFileBuilder(".", file.getPath()).build()); // "." here is to avoid this helper class's behavior of injecting an extra directory into the temp path

    ((AbstractCrossFileCheck)check).validate(textSourceFile, projectKey);

    return textSourceFile;
  }

}
