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
package it;

import com.sonar.orchestrator.Orchestrator;
import it.actionPlan.ActionPlanTest;
import it.administration.UsersPageTest;
import it.authorisation.DashboardSharingPermissionTest;
import it.authorisation.ExecuteAnalysisPermissionTest;
import it.authorisation.IssuePermissionTest;
import it.authorisation.PermissionSearchTest;
import it.authorisation.ProvisioningPermissionTest;
import it.authorisation.QualityProfileAdminPermissionTest;
import it.authorisation.SystemAdminPermissionTest;
import it.customMeasure.CustomMeasuresTest;
import it.i18n.I18nTest;
import it.measure.MeasuresWsTest;
import it.measure.NewDebtRatioMeasureTest;
import it.measure.ProjectDrilldownTest;
import it.measure.ProjectOverviewTest;
import it.measure.ProjectWidgetsTest;
import it.measure.TechnicalDebtMeasureVariationTest;
import it.measureFilter.MeasureFiltersTest;
import it.measureHistory.DifferentialPeriodsTest;
import it.measureHistory.HistoryUiTest;
import it.measureHistory.SincePreviousVersionHistoryTest;
import it.measureHistory.SinceXDaysHistoryTest;
import it.measureHistory.TimeMachineTest;
import it.projectAdministration.BackgroundTasksTest;
import it.projectAdministration.BulkDeletionTest;
import it.projectAdministration.ProjectAdministrationTest;
import it.qualityGate.QualityGateNotificationTest;
import it.qualityGate.QualityGateTest;
import it.qualityGate.QualityGateUiTest;
import it.settings.PropertySetsTest;
import it.settings.SettingsTest;
import it.settings.SubCategoriesTest;
import it.sourceCode.EncodingTest;
import it.sourceCode.HighlightingTest;
import it.sourceCode.ProjectCodeTest;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static util.ItUtils.pluginArtifact;
import static util.ItUtils.xooPlugin;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  // administration
  UsersPageTest.class,
  // project administration
  BulkDeletionTest.class,
  ProjectAdministrationTest.class,
  BackgroundTasksTest.class,
  // settings
  PropertySetsTest.class,
  SubCategoriesTest.class,
  SettingsTest.class,
  // i18n
  I18nTest.class,
  // quality gate
  QualityGateTest.class,
  QualityGateUiTest.class,
  QualityGateNotificationTest.class,
  // authorisation
  DashboardSharingPermissionTest.class,
  ExecuteAnalysisPermissionTest.class,
  IssuePermissionTest.class,
  PermissionSearchTest.class,
  ProvisioningPermissionTest.class,
  QualityProfileAdminPermissionTest.class,
  SystemAdminPermissionTest.class,
  // custom measure
  CustomMeasuresTest.class,
  // measure
  NewDebtRatioMeasureTest.class,
  ProjectDrilldownTest.class,
  ProjectOverviewTest.class,
  ProjectWidgetsTest.class,
  MeasureFiltersTest.class,
  MeasuresWsTest.class,
  TechnicalDebtMeasureVariationTest.class,
  // measure history
  DifferentialPeriodsTest.class,
  HistoryUiTest.class,
  SincePreviousVersionHistoryTest.class,
  SinceXDaysHistoryTest.class,
  TimeMachineTest.class,
  // action plan
  ActionPlanTest.class,
  // source code
  EncodingTest.class,
  HighlightingTest.class,
  ProjectCodeTest.class
})
public class Category1Suite {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Orchestrator.builderEnv()
    .setServerProperty("sonar.notifications.delay", "1")
    .addPlugin(pluginArtifact("property-sets-plugin"))
    .addPlugin(pluginArtifact("sonar-subcategories-plugin"))

    // Used in I18nTest
    .addPlugin(pluginArtifact("l10n-fr-pack"))

    // 1 second. Required for notification test.
    .setServerProperty("sonar.notifications.delay", "1")

    // Used in SettingsTest.global_property_change_extension_point
    .addPlugin(pluginArtifact("global-property-change-plugin"))

    // Used in SettingsTest.should_get_settings_default_value
    .addPlugin(pluginArtifact("server-plugin"))

    .addPlugin(xooPlugin())
    .build();

}
