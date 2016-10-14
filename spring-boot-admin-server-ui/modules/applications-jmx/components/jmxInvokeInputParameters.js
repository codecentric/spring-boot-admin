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
  require: {
    invocation: '^sbaJmxInvocation'
  },
  controller: function () {
    var ctrl = this;
    ctrl.$onInit = function () {
      ctrl.arguments = new Array(ctrl.invocation.descriptor.args.length);
      ctrl.errors = new Array(ctrl.invocation.descriptor.args.length);
    };
    ctrl.applyParameters = function () {
      var hasError = ctrl.arguments.find(function (arg) {
        return arg !== null && arg.error !== null;
      });
      if (!hasError) {
        ctrl.invocation.arguments = ctrl.arguments.map(function (arg) {
          return arg.value;
        });
        ctrl.invocation.proceedJmxInvocation();
      }
    };
    ctrl.updateArg = function (index, value, error) {
      ctrl.arguments[index] = { value: value, error: error };
    };
  },
  template: require('./jmxInvokeInputParameters.tpl.html')
};
