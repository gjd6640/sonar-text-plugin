package org.sonar.plugins.txt.language;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.txt.TextLanguage;
import org.sonar.plugins.txt.TextPlugin;

@RunWith(MockitoJUnitRunner.class)
public class TextLanguageTest {
  @Mock Configuration config;

  @Test
  public void verifyUserSpecifiedFileSuffixes() {
    when(config.getStringArray(eq(TextPlugin.FILE_SUFFIXES_KEY))).thenReturn(new String[] {".hiya"," .there"}); // There's a space here on purpose to verify that if the user adds one it'll be trimmed

	  TextLanguage t = new TextLanguage(config);

	  // Run
	  String[] fileSuffixes = t.getFileSuffixes();

	  // Check
	  System.out.println(Arrays.toString(fileSuffixes));
	  assertTrue(".hiya".equals(fileSuffixes[0]));
	  assertTrue(".there".equals(fileSuffixes[1]));
  }

  @Test
  public void userSpecifiedFileSuffixStringIsBlanks() {
    when(config.getStringArray(eq(TextPlugin.FILE_SUFFIXES_KEY))).thenReturn(new String[] {"      "," "}); // There's a space here on purpose to verify that if the user adds one it'll be trimmed

	  TextLanguage t = new TextLanguage(config);

	  // Run
	  String[] fileSuffixes = t.getFileSuffixes();

	  // Check
	  System.out.println(Arrays.toString(fileSuffixes));
	  assertTrue(".properties".equals(fileSuffixes[0]));
	  assertTrue(".txt".equals(fileSuffixes[1]));
  }

  @Test
  public void verifyDefaultFileSuffixes() {
    when(config.getStringArray(eq(TextPlugin.FILE_SUFFIXES_KEY))).thenReturn(new String[] {});

    TextLanguage t = new TextLanguage(config);

	  String[] fileSuffixes = t.getFileSuffixes();
	  System.out.println(Arrays.toString(fileSuffixes));
	  assertTrue(".properties".equals(fileSuffixes[0]));
	  assertTrue(".txt".equals(fileSuffixes[1]));
  }
}
