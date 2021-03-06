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
import qs from 'querystring';
import _ from 'underscore';
import classNames from 'classnames';
import React from 'react';

import LinksMixin from '../links-mixin';
import { translate, getLocalizedDashboardName } from '../../../helpers/l10n';
import {
    getComponentDashboardUrl,
    getComponentFixedDashboardUrl,
    getComponentDashboardManagementUrl
} from '../../../helpers/urls';


const FIXED_DASHBOARDS = [
  { link: '', name: 'overview.page' },
  { link: '/debt', name: 'overview.domain.debt' },
  { link: '/coverage', name: 'overview.domain.coverage' },
  { link: '/duplications', name: 'overview.domain.duplications' },
  { link: '/structure', name: 'overview.domain.structure' }
];

const SETTINGS_URLS = [
  '/project/settings',
  '/project/profile',
  '/project/qualitygate',
  '/custom_measures',
  '/action_plans',
  '/project/links',
  '/project_roles',
  '/project/history',
  'background_tasks',
  '/project/key',
  '/project/deletion'
];


export default React.createClass({
  mixins: [LinksMixin],

  isDeveloper() {
    const qualifier = _.last(this.props.component.breadcrumbs).qualifier;
    return qualifier === 'DEV';
  },

  isView() {
    const qualifier = _.last(this.props.component.breadcrumbs).qualifier;
    return qualifier === 'VW' || qualifier === 'SVW';
  },

  periodParameter() {
    let params = qs.parse(window.location.search.substr(1));
    return params.period ? `&period=${params.period}` : '';
  },

  getPeriod() {
    let params = qs.parse(window.location.search.substr(1));
    return params.period;
  },

  isFixedDashboardActive(fixedDashboard) {
    let path = window.location.pathname;
    return path === `/overview${fixedDashboard.link}`;
  },

  isCustomDashboardActive(customDashboard) {
    let path = window.location.pathname;
    let params = qs.parse(window.location.search.substr(1));
    return path.indexOf('/dashboard') === 0 && params['did'] === `${customDashboard.key}`;
  },

  isCustomDashboardsActive () {
    let dashboards = this.props.component.dashboards;
    return _.any(dashboards, this.isCustomDashboardActive) ||
        this.isDashboardManagementActive() ||
        this.isDefaultDeveloperDashboardActive();
  },

  isDefaultDeveloperDashboardActive() {
    let path = window.location.pathname;
    return this.isDeveloper() && path.indexOf('/dashboard') === 0;
  },

  isDashboardManagementActive () {
    let path = window.location.pathname;
    return path.indexOf('/dashboards') === 0;
  },

  renderFixedDashboards() {
    return FIXED_DASHBOARDS.map(fixedDashboard => {
      let key = 'fixed-dashboard-' + fixedDashboard.link.substr(1);
      let url = getComponentFixedDashboardUrl(this.props.component.key, fixedDashboard.link);
      let name = fixedDashboard.link !== '' ?
          translate(fixedDashboard.name) : <i className="icon-home"/>;
      let className = classNames({ active: this.isFixedDashboardActive(fixedDashboard) });
      return <li key={key} className={className}>
        <a href={url}>{name}</a>
      </li>;
    });
  },

  renderCustomDashboard(customDashboard) {
    let key = 'custom-dashboard-' + customDashboard.key;
    let url = getComponentDashboardUrl(this.props.component.key, customDashboard.key, this.getPeriod());
    let name = getLocalizedDashboardName(customDashboard.name);
    let className = classNames({ active: this.isCustomDashboardActive(customDashboard) });
    return <li key={key} className={className}>
      <a href={url}>{name}</a>
    </li>;
  },

  renderCustomDashboards() {
    let dashboards = this.props.component.dashboards.map(this.renderCustomDashboard);
    let className = classNames('dropdown', { active: this.isCustomDashboardsActive() });
    const managementLink = this.renderDashboardsManagementLink();
    return <li className={className}>
      <a className="dropdown-toggle" data-toggle="dropdown" href="#">
        {translate('layout.dashboards')}&nbsp;
        <i className="icon-dropdown"/>
      </a>
      <ul className="dropdown-menu">
        {dashboards}
        {managementLink && <li className="divider"/>}
        {managementLink}
      </ul>
    </li>;
  },

  renderDashboardsManagementLink() {
    if (!window.SS.user) {
      return null;
    }
    let key = 'dashboard-management';
    let url = getComponentDashboardManagementUrl(this.props.component.key);
    let name = translate('dashboard.manage_dashboards');
    let className = classNames('pill-right', { active: this.isDashboardManagementActive() });
    return <li key={key} className={className}>
      <a className="note" href={url}>{name}</a>
    </li>;
  },

  renderCodeLink() {
    if (this.isView() || this.isDeveloper()) {
      return null;
    }

    const url = `/code/index?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('code.page'), '/code');
  },

  renderProjectsLink() {
    if (!this.isView()) {
      return null;
    }

    const url = `/view_projects/index?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('view_projects.page'), '/view_projects');
  },

  renderComponentIssuesLink() {
    const url = `/component_issues/index?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('issues.page'), '/component_issues');
  },

  renderAdministration() {
    let shouldShowAdministration =
        this.props.conf.showActionPlans ||
        this.props.conf.showBackgroundTasks ||
        this.props.conf.showDeletion ||
        this.props.conf.showHistory ||
        this.props.conf.showLinks ||
        this.props.conf.showManualMeasures ||
        this.props.conf.showPermissions ||
        this.props.conf.showQualityGates ||
        this.props.conf.showQualityProfiles ||
        this.props.conf.showSettings ||
        this.props.conf.showUpdateKey;
    if (!shouldShowAdministration) {
      return null;
    }
    let isSettingsActive = SETTINGS_URLS.some(url => {
      return window.location.href.indexOf(url) !== -1;
    });
    let className = 'dropdown' + (isSettingsActive ? ' active' : '');
    return (
        <li className={className}>
          <a className="dropdown-toggle navbar-admin-link" data-toggle="dropdown" href="#">
            {translate('layout.settings')}&nbsp;
            <i className="icon-dropdown"/>
          </a>
          <ul className="dropdown-menu">
            {this.renderSettingsLink()}
            {this.renderProfilesLink()}
            {this.renderQualityGatesLink()}
            {this.renderCustomMeasuresLink()}
            {this.renderActionPlansLink()}
            {this.renderLinksLink()}
            {this.renderPermissionsLink()}
            {this.renderHistoryLink()}
            {this.renderBackgroundTasksLink()}
            {this.renderUpdateKeyLink()}
            {this.renderDeletionLink()}
            {this.renderExtensions()}
          </ul>
        </li>
    );
  },

  renderSettingsLink() {
    if (!this.props.conf.showSettings) {
      return null;
    }
    const url = `/project/settings?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('project_settings.page'), '/project/settings');
  },

  renderProfilesLink() {
    if (!this.props.conf.showQualityProfiles) {
      return null;
    }
    const url = `/project/profile?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('project_quality_profiles.page'), '/project/profile');
  },

  renderQualityGatesLink() {
    if (!this.props.conf.showQualityGates) {
      return null;
    }
    const url = `/project/qualitygate?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('project_quality_gate.page'), '/project/qualitygate');
  },

  renderCustomMeasuresLink() {
    if (!this.props.conf.showManualMeasures) {
      return null;
    }
    const url = `/custom_measures?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('custom_measures.page'), '/custom_measures');
  },

  renderActionPlansLink() {
    if (!this.props.conf.showActionPlans) {
      return null;
    }
    const url = `/action_plans?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('action_plans.page'), '/action_plans');
  },

  renderLinksLink() {
    if (!this.props.conf.showLinks) {
      return null;
    }
    const url = `/project/links?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('project_links.page'), '/project/links');
  },

  renderPermissionsLink() {
    if (!this.props.conf.showPermissions) {
      return null;
    }
    const url = `/project_roles?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('permissions.page'), '/project_roles');
  },

  renderHistoryLink() {
    if (!this.props.conf.showHistory) {
      return null;
    }
    const url = `/project/history?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('project_history.page'), '/project/history');
  },

  renderBackgroundTasksLink() {
    if (!this.props.conf.showBackgroundTasks) {
      return null;
    }
    const url = `/project/background_tasks?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('background_tasks.page'), '/project/background_tasks');
  },

  renderUpdateKeyLink() {
    if (!this.props.conf.showUpdateKey) {
      return null;
    }
    const url = `/project/key?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('update_key.page'), '/project/key');
  },

  renderDeletionLink() {
    if (!this.props.conf.showDeletion) {
      return null;
    }
    const url = `/project/deletion?id=${encodeURIComponent(this.props.component.key)}`;
    return this.renderLink(url, translate('deletion.page'), '/project/deletion');
  },

  renderExtensions() {
    let extensions = this.props.conf.extensions || [];
    return extensions.map(e => {
      return this.renderLink(e.url, e.name, e.url);
    });
  },

  renderTools() {
    let component = this.props.component;
    if (!component.isComparable && !_.size(component.extensions)) {
      return null;
    }
    let tools = [];
    (component.extensions || []).forEach(e => {
      tools.push(this.renderLink(e.url, e.name));
    });
    return tools;
  },

  render() {
    return (
        <ul className="nav navbar-nav nav-tabs">
          {!this.isDeveloper() && this.renderFixedDashboards()}
          {this.renderCustomDashboards()}
          {this.renderCodeLink()}
          {this.renderProjectsLink()}
          {this.renderComponentIssuesLink()}
          {this.renderTools()}
          {this.renderAdministration()}
        </ul>
    );
  }
});
