package org.sonar.plugins.text;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.plugins.text.checks.CheckRepository;
import org.sonar.squidbridge.annotations.AnnotationBasedRulesDefinition;

public final class TextRulesDefinition implements RulesDefinition {

  @Override
  public void define(Context context) {
    NewRepository repository = context
      .createRepository(CheckRepository.REPOSITORY_KEY, TextLanguage.KEY)
      .setName(CheckRepository.REPOSITORY_NAME);

    new AnnotationBasedRulesDefinition(repository, TextLanguage.KEY).addRuleClasses(false, CheckRepository.getCheckClasses());

    repository.done();
  }

}
