package org.sonar.plugins.text;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonarsource.analyzer.commons.BuiltInQualityProfileJsonLoader;

public final class TextSonarWayProfile implements BuiltInQualityProfilesDefinition {

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile sonarWay = context.createBuiltInQualityProfile(TextPlugin.SONAR_WAY_PROFILE_NAME, TextLanguage.LANGUAGE_KEY);
    BuiltInQualityProfileJsonLoader.load(sonarWay, TextRulesDefinition.REPOSITORY_KEY, TextPlugin.SONAR_WAY_JSON_FILE_PATH);
    sonarWay.done();
  }

}
