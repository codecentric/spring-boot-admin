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
    value: '=model',
    disabled: '<disabled',
    type: '@type'
  },
  controller: function ($scope) {
    'ngInject';

    var ctrl = this;
    ctrl.jsonEdited = function () {
      ctrl.value = angular.fromJson($scope.json);
    };

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
      })();

      ctrl.selectOptions = (function () {
        switch (ctrl.type) {
          case 'boolean':
            return [true, false];
          case 'java.lang.Boolean':
            return [null, true, false];
          default:
            return null;
        }
      })();

      ctrl.isObject = ctrl.selectOptions === null && ctrl.inputType === null;
      if (ctrl.isObject) {
        $scope.$watch('$ctrl.value', function (value) {
          ctrl.json = angular.toJson(value, true);
        });
      }
    };
  },
  template: require('./javaTypeInput.tpl.html')
};
