/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

module.exports = function ($rootScope, jolokia, $q) {
  'ngInject';

  function getBeanName(nameProps) {
    var name = nameProps.name || nameProps.title || null;
    var type = nameProps.type || nameProps.j2eetype || null;
    var typeText = null;
    if (type) {
      typeText = '[' + type + ']';
    }
    var props = [];
    for (var prop in nameProps) {
      if (nameProps.hasOwnProperty(prop) && prop !== 'name' && prop !== 'title' && prop !== 'type' && prop !== 'j2eetype') {
        props.push(prop + ': ' + nameProps[prop]);
      }
    }
    var propText = null;
    if (props.length > 0) {
      propText = '(' + props.join(', ') + ')';
    }
    return [name, typeText, propText].filter(function (v) {
      return v !== null;
    }).join(' ');
  }

  this.list = function (app) {
    return jolokia.list('api/applications/' + app.id + '/jolokia/').then(
      function (response) {
        var domains = [];
        for (var rDomainName in response.value) {
          if (!response.value.hasOwnProperty(rDomainName)) {
            continue;
          }

          var rDomain = response.value[rDomainName];
          var domain = {
            name: rDomainName,
            beans: []
          };

          for (var rBeanName in rDomain) {
            if (!rDomain.hasOwnProperty(rBeanName)) {
              continue;
            }
            var rBean = rDomain[rBeanName];
            var bean = {
              id: domain.name + ':' + rBeanName,
              name: '',
              domain: domain.name,
              nameProps: {},
              description: rBean.desc,
              operations: rBean.op,
              attributes: rBean.attr
            };

            var parts = rBeanName.split(',');
            for (var i = 0; i < parts.length; i++) {
              var tokens = parts[i].split('=');
              bean.nameProps[tokens[0].toLowerCase()] = tokens[1];
            }
            bean.name = getBeanName(bean.nameProps);
            domain.beans.push(bean);
          }
          domains.push(domain);
        }

        return domains;
      }
    ).catch(function (response) {
      return $q.reject(response);
    });
  };

  this.readAllAttr = function (app, bean) {
    return jolokia.read('api/applications/' + app.id + '/jolokia/', bean.id);
  };

  this.writeAttr = function (app, bean, attr, val) {
    return jolokia.writeAttr('api/applications/' + app.id + '/jolokia/', bean.id, attr, val);
  };

  this.invoke = function (app, bean, opname, args) {
    return jolokia.exec('api/applications/' + app.id + '/jolokia/', bean.id, opname, args);
  };
};
