package org.sonar.plugins.text.language;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Optional;

import org.junit.Test;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.text.TextLanguage;
import org.sonar.plugins.text.TextPlugin;

public class TextLanguageTest {

  @Test
  public void verifyDefaultFileSuffixes() {

	  TextLanguage t = new TextLanguage(createMockConfig());

	  String[] fileSuffixes = t.getFileSuffixes();
	  System.out.println(Arrays.toString(fileSuffixes));
	  assertTrue(".properties".equals(fileSuffixes[0]));
	  assertTrue(".txt".equals(fileSuffixes[1]));
  }

  private Configuration createMockConfig() {
    return new Configuration() {

      @Override
      public Optional<String> get(String key) {
        if (TextPlugin.FILE_SUFFIXES_KEY.equals(key)) {
          return Optional.of(".properties, .txt");
        }
        return Optional.empty();
      }

      @Override
      public boolean hasKey(String key) {
        return TextPlugin.FILE_SUFFIXES_KEY.equals(key);
      }

      @Override
      public String[] getStringArray(String key) {
        return new String[] {".properties"," .txt"}; // There's a space here on purpose to verify that if the user adds one it'll be trimmed
      }
    };
  }

}
