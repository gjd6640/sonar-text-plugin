package org.sonar.plugins.text.language;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.plugins.text.TextLanguage;
import org.sonar.plugins.text.TextPlugin;

public class TextLanguageTest {

  @Test
  public void verifyUserSpecifiedFileSuffixes() {
	  Settings s = new Settings();
	  TextLanguage t = new TextLanguage(s);

	  // Run
	  s.setProperty(TextPlugin.FILE_SUFFIXES_KEY, ".hiya, .there"); // There's a space here on purpose to verify that if the user adds one it'll be trimmed
	  String[] fileSuffixes = t.getFileSuffixes();
	  
	  // Check
	  System.out.println(Arrays.toString(fileSuffixes));
	  assertTrue(".hiya".equals(fileSuffixes[0]));
	  assertTrue(".there".equals(fileSuffixes[1]));
  }
  
  @Test
  public void userSpecifiedFileSuffixStringIsBlanks() {
	  Settings s = new Settings();
	  TextLanguage t = new TextLanguage(s);

	  // Run
	  s.setProperty(TextPlugin.FILE_SUFFIXES_KEY, "   "); // There's a space here on purpose to verify that if the user adds one it'll be trimmed
	  String[] fileSuffixes = t.getFileSuffixes();
	  
	  // Check
	  System.out.println(Arrays.toString(fileSuffixes));
	  assertTrue(".properties".equals(fileSuffixes[0]));
	  assertTrue(".txt".equals(fileSuffixes[1]));
  }
	  
  @Test
  public void verifyDefaultFileSuffixes() {
	  Settings s = new Settings();
	  TextLanguage t = new TextLanguage(s);
	  
	  String[] fileSuffixes = t.getFileSuffixes();
	  System.out.println(Arrays.toString(fileSuffixes));
	  assertTrue(".properties".equals(fileSuffixes[0]));
	  assertTrue(".txt".equals(fileSuffixes[1]));
  }
}
