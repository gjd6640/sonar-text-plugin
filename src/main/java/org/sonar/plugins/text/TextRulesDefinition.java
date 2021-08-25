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

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(REPOSITORY_KEY, TextLanguage.LANGUAGE_KEY).setName("Sonar Text Plugin");

    RulesDefinitionAnnotationLoader rulesLoader = new RulesDefinitionAnnotationLoader();
    rulesLoader.load(repository, TextChecksList.getChecksClassArray());

    for (NewRule rule : repository.rules()) {
      rule.setTemplate(true);
    }

    // Finalize the definition procedure
    repository.done();
  }
}
