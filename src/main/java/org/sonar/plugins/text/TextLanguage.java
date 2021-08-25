package org.sonar.plugins.text;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

import com.google.common.collect.Lists;

public class TextLanguage extends AbstractLanguage {

  protected static final String DEFAULT_SUFFIXES = ".properties,.txt";
  public static final String LANGUAGE_KEY = "text";
  private static final String TEXT_LANGUAGE_NAME = "text";

  private final Configuration configuration;

  public TextLanguage(final Configuration configuration) {
    super(LANGUAGE_KEY, TEXT_LANGUAGE_NAME);
    this.configuration = configuration;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getFileSuffixes() {
    String[] suffixes = filterEmptyStrings(configuration.getStringArray(TextPlugin.FILE_SUFFIXES_KEY));
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
