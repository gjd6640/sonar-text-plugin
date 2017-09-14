package org.sonar.plugins.text.checks;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class CheckRepository {

  public static final String REPOSITORY_KEY = "text";
  public static final String REPOSITORY_NAME = "SonarQube";
  public static final String SONAR_WAY_PROFILE_NAME = "Sonar way";

  private CheckRepository() {
  }

  public static List<AbstractTextCheck> getChecks() {
    return ImmutableList.of(
        new SimpleTextMatchCheck(),
        new RequiredStringNotPresentCheck(),
        new MultilineTextMatchCheck(),
        new StringDisallowedIfMatchInAnotherFileCheck(),
        new MultiFileIfOneStringExistsThenBothMustExistCheck()
      );
  }

  public static List<Class> getCheckClasses() {
    ImmutableList.Builder<Class> builder = ImmutableList.builder();

    for (AbstractTextCheck check : getChecks()) {
      builder.add(check.getClass());
    }

    return builder.build();
  }

}
