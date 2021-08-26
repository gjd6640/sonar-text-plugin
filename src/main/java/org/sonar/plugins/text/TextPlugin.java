package org.sonar.plugins.text;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.text.batch.TextIssueSensor;

public final class TextPlugin implements Plugin {

  public static final String MY_PROPERTY = "sonar.example.myproperty";
  public static final String FILE_SUFFIXES_KEY = "sonar-text-plugin.file.suffixes";
  public static final String SONAR_WAY_PROFILE_NAME = "Community Text Plugin way";
  public static final String SONAR_WAY_JSON_FILE_PATH = "org/sonar/l10n/text/default_quality_profile/Sonar_way_profile.json";

  @Override
  public void define(Context context) {
    context.addExtensions(
        PropertyDefinition.builder(TextPlugin.FILE_SUFFIXES_KEY)
          .name("File suffixes")
          .description("List of suffixes of files to analyze.")
          .defaultValue(TextLanguage.DEFAULT_SUFFIXES)
          .multiValues(true)
          .category("Text")
          .build(),
          TextIssueSensor.class,
          TextLanguage.class,
          TextRulesDefinition.class,
          TextSonarWayProfile.class
        );
  }
}
