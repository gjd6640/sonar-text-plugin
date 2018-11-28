package org.sonar.plugins.text;

import java.util.Arrays;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.text.batch.TextIssueSensor;

@Properties({
	@Property(
		    key = TextPlugin.FILE_SUFFIXES_KEY,
		    defaultValue = TextLanguage.DEFAULT_SUFFIXES,
		    name = "File suffixes",
		    description = "Comma-separated list of suffixes for files to analyze.",
		    global = true,
		    project = false)
	})
public final class TextPlugin implements Plugin {

  public static final String MY_PROPERTY = "sonar.example.myproperty";

  public static final String FILE_SUFFIXES_KEY = "sonar-text-plugin.file.suffixes";

  @Override
  public void define(Context context) {
    context.addExtensions(Arrays.asList(
          TextIssueSensor.class,
          TextLanguage.class,
          TextRulesDefinition.class,
          TextQualityProfile.class
        ));
  }
}
