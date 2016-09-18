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

  this.list = function (app) {
    return jolokia.list('api/applications/' + app.id + '/jolokia/').then(
      function (response) {
        var domains = [];
        for (var rDomainName in response.value) {
          var rDomain = response.value[rDomainName];
          var domain = {
            name: rDomainName,
            beans: []
          };

          for (var rBeanName in rDomain) {
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
            var name = '';
            var type = '';
            var parts = rBeanName.split(',');
            for (var i = 0; i < parts.length; i++) {
              var tokens = parts[i].split('=');
              if (tokens[0].toLowerCase() === 'name') {
                name = tokens[1];
              } else {
                bean.nameProps[tokens[0]] = tokens[1];
                if ((tokens[0].toLowerCase() === 'type' || tokens[0]
                    .toLowerCase() === 'j2eetype') && type.length === 0) {
                  type = tokens[1];
                }
              }
            }

            if (name.length !== 0) {
              bean.name = name;
            }
            if (type.length !== 0) {
              if (bean.name !== 0) {
                bean.name += ' ';
              }
              bean.name += '[' + type + ']';
            }

            if (bean.name.length === 0) {
              bean.name = rBeanName;
            }

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
