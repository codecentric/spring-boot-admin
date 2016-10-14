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

var angular = require('angular');
module.exports = {
  bindings: {
    bean: '<bean',
    application: '<application'
  },
  controller: function (ApplicationJmx) {
    'ngInject';

    var ctrl = this;
    ctrl.invocation = null;
    ctrl.$onChanges = function () {
      if (ctrl.bean) {
        ctrl.initAttributes();
        ctrl.readAttributeValues();
      } else {
        ctrl.attributes = {};
        ctrl.error = null;
      }
    };

    ctrl.initAttributes = function () {
      ctrl.attributes = {};
      angular.forEach(ctrl.bean.attributes, function (descriptor, name) {
        ctrl.attributes[name] = { value: null, error: null, descriptor: descriptor };
      });
    };

    ctrl.readAttributeValues = function () {
      ctrl.readingAttributes = true;
      ctrl.error = null;
      ApplicationJmx.readAllAttr(ctrl.application, ctrl.bean).then(
        function (response) {
          angular.forEach(response.value, function (value, name) {
            ctrl.attributes[name].value = value;
          });
          ctrl.readingAttributes = false;
        }
      ).catch(function (response) {
        ctrl.error = response.error;
        ctrl.readingAttributes = false;
      });
    };

    ctrl.updateAttributeValue = function (name, value, error) {
      ctrl.attributes[name].value = value;
      ctrl.attributes[name].error = error;
    };

    ctrl.writeAttributeValue = function (name) {
      ctrl.attributes[name].error = null;
      ApplicationJmx.writeAttr(ctrl.application, ctrl.bean, name, ctrl.attributes[name].value).catch(
        function (response) {
          ctrl.attributes[name].error = response.error;
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
