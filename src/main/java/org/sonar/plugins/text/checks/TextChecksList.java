package org.sonar.plugins.text.checks;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class TextChecksList {

  public static final String REPOSITORY_KEY = "text";
  public static final String REPOSITORY_NAME = "SonarQube";
  public static final String SONAR_WAY_PROFILE_NAME = "Sonar way";

  private TextChecksList() {
  }

  public static Class<?>[] getChecksClassArray() {
    return getCheckClasses().toArray(new Class[0]);
  }

  public static List<AbstractTextCheck> getChecks() {
    // TODO: Consider removing usage of ImmutableList here as it is the only reason that we have a guava dependency...
    return ImmutableList.of(
        new SimpleTextMatchCheck(),
        new RequiredStringNotPresentCheck(),
        new MultilineTextMatchCheck(),
        new StringDisallowedIfMatchInAnotherFileCheck(),
        new MultiFileIfOneStringExistsThenBothMustExistCheck()
      );
  }

  public static List<Class<?>> getCheckClasses() {
    ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();

    for (AbstractTextCheck check : getChecks()) {
      builder.add(check.getClass());
    }

    return builder.build();
  }

}
