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
    value: '<sbaValue',
    disabled: '<sbaDisabled',
    type: '@sbaType',
    onChange: '&sbaChange'
  },
  controller: function () {
    'ngInject';

    var ctrl = this;

    ctrl.$onInit = function () {
      ctrl.inputType = (function () {
        switch (ctrl.type) {
          case 'java.lang.String':
            return 'text';
          case 'int':
          case 'java.lang.Integer':
          case 'long':
          case 'java.lang.Long':
          case 'double':
          case 'java.lang.Double':
          case 'float':
          case 'java.lang.Float':
          case 'short':
          case 'java.lang.short':
          case 'java.lang.Number':
            return 'number';
          default:
            return null;
        }
      } ());

      ctrl.selectOptions = (function () {
        switch (ctrl.type) {
          case 'boolean':
            return [true, false];
          case 'java.lang.Boolean':
            return [null, true, false];
          default:
            return null;
        }
      } ());

      ctrl.isObject = ctrl.selectOptions === null && ctrl.inputType === null;
    };

    ctrl.$onChanges = function () {
      if (ctrl.isObject) {
        ctrl.json = angular.toJson(ctrl.value, true);
      }
    };

    ctrl.valueChanged = function () {
      ctrl.onChange({ value: ctrl.value, error: null });
    };

    ctrl.jsonChanged = function () {
      try {
        ctrl.onChange({ value: angular.fromJson(ctrl.json), error: null });
      } catch (error) {
        ctrl.onChange({ value: null, error: error });
      }
    };
  },
  template: require('./javaTypeInput.tpl.html')
};
