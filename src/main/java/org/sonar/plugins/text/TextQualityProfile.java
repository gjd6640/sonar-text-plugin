package org.sonar.plugins.text;

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

public class TextQualityProfile implements BuiltInQualityProfilesDefinition {
  public static final String BUILTIN_QUALITY_PROFILE_NAME = "Sonar way";

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(BUILTIN_QUALITY_PROFILE_NAME, TextLanguage.LANGUAGE_KEY);
    profile.setDefault(true);

//    NewBuiltInActiveRule rule1 = profile.activateRule(CheckRepository.REPOSITORY_KEY, "ExampleRule1");
//    rule1.overrideSeverity("BLOCKER");

    profile.done();
  }

}
