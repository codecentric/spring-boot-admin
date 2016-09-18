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

module.exports = {
  bindings: {
    bean: '<bean',
    application: '<application'
  },
  controller: function (ApplicationJmx) {
    'ngInject';

    var ctrl = this;
    ctrl.attributeValues = {};
    ctrl.attributeErrors = {};
    ctrl.error = null;
    ctrl.invocation = null;

    ctrl.readAttributeValues = function () {
      ctrl.readingAttributes = true;
      ctrl.error = null;
      ctrl.attributeErrors = {};
      ctrl.attributeValues = {};
      ApplicationJmx.readAllAttr(ctrl.application, ctrl.bean).then(
        function (response) {
          ctrl.attributeValues = response.value;
          ctrl.readingAttributes = false;
        }
      ).catch(function (response) {
        ctrl.error = response.error;
        ctrl.readingAttributes = false;
      });
    };

    ctrl.writeAttributeValue = function (name, value) {
      ctrl.attributeErrors[name] = null;
      ApplicationJmx.writeAttr(ctrl.application, ctrl.bean, name, value).catch(
        function (response) {
          ctrl.attributeErrors[name] = response.error;
        });
    };

    ctrl.doInvoke = function (name, operation) {
      ctrl.invocation = {
        name: name,
        operation: operation
      };
    };

    ctrl.cancelInvoke = function () {
      ctrl.invocation = null;
    };
  },
  template: require('./jmxBean.tpl.html')
};
