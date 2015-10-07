package org.sonar.plugins.text.checks.util;

public class LineNumberFinderUtil {
  public static int countLines(String str, int stopAtPosition) {
    if(str == null || str.isEmpty()) {
        return 0;
    }
    int lines = 1;
    int pos = 0;
    while ((pos = str.indexOf("\n", pos) + 1) != 0 && pos <= stopAtPosition) {
        lines++;
    }
    return lines;
  }
}
