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
package org.sonar.plugins.text;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.AbstractLanguage;

import java.util.List;

public class TextLanguage extends AbstractLanguage {

  protected static final String DEFAULT_SUFFIXES = ".properties,.txt";

  public static final String KEY = "text";

  private static final String TEXT_LANGUAGE_NAME = "text";

  private Settings settings;

  public TextLanguage(Settings settings) {
    super(KEY, TEXT_LANGUAGE_NAME);
    this.settings = settings;
  }

  /**
   * {@inheritDoc}
   */
  public String[] getFileSuffixes() {
    String[] suffixes = filterEmptyStrings(settings.getStringArray(TextPlugin.FILE_SUFFIXES_KEY));
    if (suffixes.length == 0) {
      suffixes = TextLanguage.DEFAULT_SUFFIXES.split("\\s*,\\s*");
    }
    return suffixes;
  }

  private static String[] filterEmptyStrings(String[] stringArray) {
    List<String> nonEmptyStrings = Lists.newArrayList();
    for (String string : stringArray) {
      if (StringUtils.isNotBlank(string.trim())) {
        nonEmptyStrings.add(string.trim());
      }
    }
    return nonEmptyStrings.toArray(new String[nonEmptyStrings.size()]);
  }

}
