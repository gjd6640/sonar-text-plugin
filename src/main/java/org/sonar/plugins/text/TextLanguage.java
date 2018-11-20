package org.sonar.plugins.text;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

import com.google.common.collect.Lists;

public class TextLanguage extends AbstractLanguage {

  protected static final String DEFAULT_SUFFIXES = ".properties,.txt";

  public static final String LANGUAGE_KEY = "text";

  private static final String TEXT_LANGUAGE_NAME = "text";

  private final Configuration settings;

  public TextLanguage(final Configuration settings) {
    super(LANGUAGE_KEY, TEXT_LANGUAGE_NAME);
    this.settings = settings;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = filterEmptyStrings(settings.getStringArray(TextPlugin.FILE_SUFFIXES_KEY));
    if (suffixes.length == 0) {
      suffixes = TextLanguage.DEFAULT_SUFFIXES.split("\\s*,\\s*");
    }
    return suffixes;
  }

  private static String[] filterEmptyStrings(final String[] stringArray) {
    List<String> nonEmptyStrings = Lists.newArrayList();
    for (String string : stringArray) {
      if (StringUtils.isNotBlank(string.trim())) {
        nonEmptyStrings.add(string.trim());
      }
    }
    return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
  }

}
