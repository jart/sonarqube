/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
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
package org.sonar.server.startup;

import org.picocontainer.Startable;
import org.sonar.api.utils.System2;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.db.DbClient;
import org.sonar.db.DbSession;
import org.sonar.db.loadedtemplate.LoadedTemplateDto;
import org.sonar.db.rule.RuleDto;
import org.sonar.server.rule.index.RuleIndexer;

import static org.sonar.db.loadedtemplate.LoadedTemplateDto.ONE_SHOT_TASK_TYPE;

/**
 * Clear the overloaded technical debt of rules when SQALE plugin is not installed.
 * See <a href="https://jira.sonarsource.com/browse/SONAR-6547">SONAR-6547</a>.
 *
 * Should be removed after LTS 5.X
 *
 * @since 5.2
 */
public class ClearRulesOverloadedDebt implements Startable {

  private static final Logger LOG = Loggers.get(ClearRulesOverloadedDebt.class);

  private static final String TEMPLATE_KEY = "ClearRulesOverloadedDebt";

  private static final String SQALE_LICENSE_PROPERTY = "sonar.sqale.licenseHash.secured";

  private final System2 system2;

  private final DbClient dbClient;

  private final RuleIndexer ruleIndexer;

  public ClearRulesOverloadedDebt(System2 system2, DbClient dbClient, RuleIndexer ruleIndexer) {
    this.system2 = system2;
    this.dbClient = dbClient;
    this.ruleIndexer = ruleIndexer;
  }

  @Override
  public void start() {
    DbSession session = dbClient.openSession(false);
    try {
      if (hasAlreadyBeenExecuted(session)) {
        return;
      }
      if (!isSqalePluginInstalled(session)) {
        clearDebt(session);
      }
      markAsExecuted(session);
      session.commit();
      ruleIndexer.index();
    } finally {
      dbClient.closeSession(session);
    }
  }

  private void clearDebt(DbSession session) {
    int countClearedRules = 0;
    for (RuleDto rule : dbClient.ruleDao().selectAll(session)) {
      if (isDebtOverridden(rule)) {
        rule.setRemediationFunction(null);
        rule.setRemediationCoefficient(null);
        rule.setRemediationOffset(null);
        rule.setUpdatedAt(system2.now());
        dbClient.ruleDao().update(session, rule);
        countClearedRules++;
      }
    }
    if (countClearedRules > 0) {
      LOG.warn("The SQALE model has been cleaned to remove any redundant data left over from previous migrations.");
      LOG.warn("=> As a result, the technical debt of existing issues in your projects may change slightly when those projects are reanalyzed.");
    }
  }

  private static boolean isDebtOverridden(RuleDto ruleDto) {
    return ruleDto.getRemediationFunction() != null || ruleDto.getRemediationCoefficient() != null
      || ruleDto.getRemediationOffset() != null;
  }

  private boolean isSqalePluginInstalled(DbSession session) {
    return dbClient.propertiesDao().selectGlobalProperty(session, SQALE_LICENSE_PROPERTY) != null;
  }

  private boolean hasAlreadyBeenExecuted(DbSession session) {
    return dbClient.loadedTemplateDao().countByTypeAndKey(ONE_SHOT_TASK_TYPE, TEMPLATE_KEY, session) > 0;
  }

  private void markAsExecuted(DbSession session) {
    dbClient.loadedTemplateDao().insert(new LoadedTemplateDto(TEMPLATE_KEY, ONE_SHOT_TASK_TYPE), session);
  }

  @Override
  public void stop() {
    // Nothing to do
  }
}
