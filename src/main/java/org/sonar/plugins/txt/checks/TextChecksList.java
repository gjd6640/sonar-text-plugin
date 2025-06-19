package org.sonar.plugins.txt.checks;

import java.util.Arrays;
import java.util.List;

public class TextChecksList {

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
