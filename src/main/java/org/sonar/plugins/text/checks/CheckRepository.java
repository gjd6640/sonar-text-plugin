package org.sonar.plugins.text.checks;

import java.util.List;

import com.google.common.collect.ImmutableList;

public class CheckRepository {

  public static final String REPOSITORY_KEY = "text";
  public static final String REPOSITORY_NAME = "SonarQube";
  public static final String SONAR_WAY_PROFILE_NAME = "Sonar way";

  private CheckRepository() {
  }

  public static List<SimpleTextMatchCheck> getChecks() {
    return ImmutableList.of(
      new SimpleTextMatchCheck()
//      new FancyOtherCheck(), // When you add a second check you'll also change the return type to the checks' abstract parent class
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
