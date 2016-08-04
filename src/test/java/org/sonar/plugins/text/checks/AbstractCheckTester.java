/*
 * SonarQube XML Plugin
 * Copyright (C) 2010 SonarSource
 * sonarqube@googlegroups.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
