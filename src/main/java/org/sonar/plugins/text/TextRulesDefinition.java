/*
 * Example Plugin for SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.text;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.api.server.rule.RulesDefinitionAnnotationLoader;
import org.sonar.plugins.text.checks.TextChecksList;

public class TextRulesDefinition implements RulesDefinition {
  public static final String REPOSITORY_KEY = "text";
//  public static final RuleKey RULE_ON_LINE_1 = RuleKey.of(REPOSITORY_KEY, "line1");

//  private static final String RULE_RESOURCES_PATH = "org/sonar/l10n/text/rules/";

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(REPOSITORY_KEY, TextLanguage.LANGUAGE_KEY).setName("Sonar Text Plugin");

/*
    RuleMetadataLoader ruleMetadataLoader = new RuleMetadataLoader(RULE_RESOURCES_PATH, TextPlugin.SONAR_WAY_JSON_FILE_PATH);
    ruleMetadataLoader.addRulesByAnnotatedClass(repository, TextChecksList.getCheckClasses());
    //repository.rule("XPathCheck").setTemplate(true);
*/
    RulesDefinitionAnnotationLoader rulesLoader = new RulesDefinitionAnnotationLoader();
    rulesLoader.load(repository, TextChecksList.getChecksClassArray());
    /*
    for(AbstractTextCheck check : TextChecksList.getChecks()) {
      rulesLoader.load(repository, check.getClass());
    }
     */

    for (NewRule rule : repository.rules()) {
      rule.setTemplate(true);
    }
    /*
    NewRule x1Rule = repository.createRule(RULE_ON_LINE_1.rule())
      .setName("Stupid rule")
      .setHtmlDescription("Generates an issue on every line 1 of Java files")

      // optional tags
      .setTags("style", "stupid")

      // optional status. Default value is READY.
      .setStatus(RuleStatus.BETA)

      // default severity when the rule is activated on a Quality profile. Default value is MAJOR.
      .setSeverity(Severity.MINOR);

    x1Rule.setDebtRemediationFunction(x1Rule.debtRemediationFunctions().linearWithOffset("1h", "30min"));
*/

    // don't forget to call done() to finalize the definition
    repository.done();
  }
}
