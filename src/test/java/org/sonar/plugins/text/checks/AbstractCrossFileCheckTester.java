package org.sonar.plugins.text.checks;

import java.io.File;

import org.sonar.api.batch.fs.internal.DefaultInputFile;

public abstract class AbstractCrossFileCheckTester extends AbstractCheckTester {

  @Override
  protected TextSourceFile parseAndCheck(final File file, final AbstractTextCheck check, final String projectKey) {
    if (check instanceof AbstractCrossFileCheck == false) {
      throw new IllegalArgumentException("This abstract class is only for use with cross-file checks");
    }
    TextSourceFile textSourceFile = new TextSourceFile(new DefaultInputFile(file.getPath()).setAbsolutePath(file.getAbsolutePath()));

    ((AbstractCrossFileCheck)check).validate(textSourceFile, projectKey);

    return textSourceFile;
  }

}
