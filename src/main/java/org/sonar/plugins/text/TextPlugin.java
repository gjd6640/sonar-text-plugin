package org.sonar.plugins.text;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.text.batch.IssueSensor;

@Properties({
	@Property(
		    key = TextPlugin.FILE_SUFFIXES_KEY,
		    defaultValue = TextLanguage.DEFAULT_SUFFIXES,
		    name = "File suffixes",
		    description = "Comma-separated list of suffixes for files to analyze.",
		    global = true,
		    project = false)
	})
public final class TextPlugin extends SonarPlugin {

  public static final String MY_PROPERTY = "sonar.example.myproperty";
  
  public static final String FILE_SUFFIXES_KEY = "sonar-text-plugin.file.suffixes";
  
  // This is where you're going to declare all your SonarQube extensions
  @Override
  public List getExtensions() {
    return Arrays.asList(
      IssueSensor.class, 
      TextLanguage.class, 
      TextRulesDefinition.class
      );
  }
}
