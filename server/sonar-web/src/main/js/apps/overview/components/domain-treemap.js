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
import _ from 'underscore';
import React from 'react';

import { Treemap } from '../../../components/charts/treemap';
import { getChildren } from '../../../api/components';
import { formatMeasure } from '../../../helpers/measures';
import { getComponentUrl } from '../../../helpers/urls';
import { translate, translateWithParameters } from '../../../helpers/l10n';


const HEIGHT = 302;


export class DomainTreemap extends React.Component {
  constructor (props) {
    super(props);
    this.state = {
      loading: true,
      files: [],
      sizeMetric: this.getMetricObject(props.metrics, props.sizeMetric),
      colorMetric: props.colorMetric ? this.getMetricObject(props.metrics, props.colorMetric) : null,
      breadcrumbs: []
    };
  }

  componentDidMount () {
    this.requestComponents(this.props.component.key);
  }

  requestComponents (componentKey) {
    let metrics = [this.props.sizeMetric, this.props.colorMetric];
    return getChildren(componentKey, metrics).then(r => {
      let components = r.map(component => {
        let measures = {};
        (component.measures || []).forEach(measure => {
          measures[measure.metric] = measure.value;
        });
        return _.extend(component, {
          measures,
          key: component.refKey || component.key
        });
      });
      this.setState({ loading: false, components });
    });
  }

  getMetricObject (metrics, metricKey) {
    return _.findWhere(metrics, { key: metricKey });
  }

  getTooltip (component) {
    let inner = [
      component.name,
      `${this.state.sizeMetric.name}:
      ${formatMeasure(component.measures[this.props.sizeMetric], this.state.sizeMetric.type)}`
    ];
    if (this.state.colorMetric) {
      let measure = component.measures[this.props.colorMetric];
      let formatted = measure != null ? formatMeasure(measure, this.state.colorMetric.type) : '—';
      inner.push(`${this.state.colorMetric.name}: ${formatted}`);
    }
    inner = inner.join('<br>');
    return `<div class="text-left">${inner}</div>`;
  }

  handleRectangleClick (node) {
    this.requestComponents(node.key).then(() => {
      let nextBreadcrumbs = [...this.state.breadcrumbs];
      let index = _.findIndex(this.state.breadcrumbs, b => b.key === node.key);
      if (index !== -1) {
        nextBreadcrumbs = nextBreadcrumbs.slice(0, index);
      }
      nextBreadcrumbs = [...nextBreadcrumbs, {
        key: node.key,
        name: node.name,
        qualifier: node.qualifier
      }];
      this.setState({ breadcrumbs: nextBreadcrumbs });
    });
  }

  handleReset() {
    this.requestComponents(this.props.component.key).then(() => {
      this.setState({ breadcrumbs: [] });
    });
  }

  renderLoading () {
    return <div className="overview-chart-placeholder" style={{ height: HEIGHT }}>
      <i className="spinner"/>
    </div>;
  }

  renderTreemap () {
    if (this.state.loading) {
      return this.renderLoading();
    }

    let items = this.state.components
        .filter(component => component.measures[this.props.sizeMetric])
        .map(component => {
          let colorMeasure = this.props.colorMetric ? component.measures[this.props.colorMetric] : null;
          return {
            key: component.key,
            name: component.name,
            qualifier: component.qualifier,
            size: component.measures[this.props.sizeMetric],
            color: colorMeasure != null ? this.props.scale(colorMeasure) : '#777',
            tooltip: this.getTooltip(component),
            label: component.name,
            link: getComponentUrl(component.key)
          };
        });

    const canBeClicked = node => node.qualifier !== 'FIL' && node.qualifier !== 'UTS';

    // FIXME remove this magic number
    const height = HEIGHT - 35;

    return <Treemap
        items={items}
        breadcrumbs={this.state.breadcrumbs}
        height={height}
        canBeClicked={canBeClicked}
        onRectangleClick={this.handleRectangleClick.bind(this)}
        onReset={this.handleReset.bind(this)}/>;
  }

  render () {
    let color = this.props.colorMetric ?
        <li>{translateWithParameters('overview.chart.legend.color_x', this.state.colorMetric.name)}</li> : null;
    return <div className="overview-domain-chart">
      <div className="overview-card-header">
        <h2 className="overview-title">{translate('overview.chart.components')}</h2>
        <ul className="list-inline small">
          <li>
            {translateWithParameters('overview.chart.legend.size_x', this.state.sizeMetric.name)}
          </li>
          {color}
        </ul>
      </div>
      <div className="overview-treemap" style={{ height: HEIGHT }}>
        {this.renderTreemap()}
      </div>
    </div>;
  }
}

DomainTreemap.propTypes = {
  sizeMetric: React.PropTypes.string.isRequired,
  colorMetric: React.PropTypes.string,
  scale: React.PropTypes.func
};
