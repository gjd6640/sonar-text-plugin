package org.sonar.plugins.txt;

import java.util.Arrays;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

public class TextLanguage extends AbstractLanguage {

  private final Configuration configuration;

  public TextLanguage(final Configuration configuration) {
    super(TextPlugin.LANGUAGE_KEY, TextPlugin.NAME);
    this.configuration = configuration;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = Arrays.stream(configuration.getStringArray(TextPlugin.FILE_SUFFIXES_KEY))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toArray(String[]::new);
    if (suffixes.length > 0) {
      return suffixes;
    }
    return TextPlugin.FILE_SUFFIXES_DEFAULT_VALUE.split(",");
  }
}
