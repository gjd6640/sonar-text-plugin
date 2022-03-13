package org.sonar.plugins.text;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.text.batch.TextIssueSensor;

public final class TextPlugin implements Plugin {

  public static final String FILE_SUFFIXES_KEY = "sonar-text-plugin.file.suffixes";
  public static final String SONAR_WAY_PROFILE_NAME = "Community Text Plugin way";
  public static final String SONAR_WAY_JSON_FILE_PATH = "org/sonar/l10n/text/default_quality_profile/Sonar_way_profile.json";
  public static final String REPOSITORY_KEY = "text"; // When SQ v9.3 added its own text language we did not re-home our rule templates. There's not a seamless way to port the custom rules to a new repository key. Users would have to export/searchAndReplace/import their quality profiles to move their custom rules.
  public static final String LANGUAGE_KEY = "text"; // We're now sharing language "text" with the built-in language that was added in SonarQube v9.3
  private static final String DEFAULT_SUFFIXES = ".properties,.txt";

  @Override
  public void define(Context context) {
    context.addExtensions(
        PropertyDefinition.builder(TextPlugin.FILE_SUFFIXES_KEY)
          .name("File suffixes")
          .description("List of suffixes of files to analyze.")
          .defaultValue(DEFAULT_SUFFIXES)
          .multiValues(true)
          .category("Text")
          .build(),
          TextIssueSensor.class,
          TextRulesDefinition.class,
          TextSonarWayProfile.class
        );
  }
}
