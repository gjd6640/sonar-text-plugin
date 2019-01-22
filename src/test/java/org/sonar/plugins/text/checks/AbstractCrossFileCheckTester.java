package org.sonar.plugins.text.checks;

import java.io.File;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

public abstract class AbstractCrossFileCheckTester extends AbstractCheckTester {

  @Override
  protected SensorContextTester getSensorContextTester() {
    return super.getSensorContextTester();
  }

  @Override
  protected TextSourceFile parseAndCheck(final File file, final AbstractTextCheck check, final String projectKey) {
    if (check instanceof AbstractCrossFileCheck == false) {
      throw new IllegalArgumentException("This abstract class is only for use with cross-file checks");
    }
    TextSourceFile textSourceFile = new TextSourceFile(new TestInputFileBuilder("blahModuleKey", file.getPath()).setType(InputFile.Type.MAIN).build());
    ((AbstractCrossFileCheck)check).validate(textSourceFile, projectKey);

    return textSourceFile;
  }

}
