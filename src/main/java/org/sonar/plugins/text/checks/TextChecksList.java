package org.sonar.plugins.text.checks;

import java.util.Arrays;
import java.util.List;

public class TextChecksList {

  public static final String REPOSITORY_KEY = "text";
  public static final String REPOSITORY_NAME = "SonarQube";
  public static final String SONAR_WAY_PROFILE_NAME = "Sonar way";

  private TextChecksList() { }

  public static Class<?>[] getChecksClassArray() {
    return getCheckClasses().toArray(new Class[0]);
  }

  public static List<Class<?>> getCheckClasses() {
    List<Class<?>> checkClassList
      = Arrays.asList(
            SimpleTextMatchCheck.class,
            RequiredStringNotPresentCheck.class,
            MultilineTextMatchCheck.class,
            StringDisallowedIfMatchInAnotherFileCheck.class,
            MultiFileIfOneStringExistsThenBothMustExistCheck.class
          );

    return checkClassList;
  }

}
