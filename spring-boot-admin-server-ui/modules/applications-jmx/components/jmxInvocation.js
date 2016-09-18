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
    name: '<operationName',
    descriptor: '<operationDescriptor',
    cancelInvoke: '&onCancel'
  },
  require: {
    parent: '^sbaJmxBean'
  },
  controller: function (ApplicationJmx) {
    'ngInject';

    var ctrl = this;
    var getSignature = function (args) {
      var types = args.map(function (arg) {
        return arg.type;
      }).join(',');
      return '(' + types + ')';
    };

    ctrl.$onInit = function () {
      ctrl.state = 'init';
      ctrl.signature = '';
      ctrl.arguments = [];
      ctrl.response = null;
      ctrl.proceedJmxInvocation();
    };

    ctrl.proceedJmxInvocation = function () {
      if (ctrl.descriptor instanceof Array) {
        ctrl.state = 'selectOverload';
        return;
      }

      ctrl.signature = ctrl.name + getSignature(ctrl.descriptor.args);
      if (ctrl.descriptor.args.length > 0 && ctrl.descriptor.args.length > ctrl.arguments.length) {
        ctrl.state = 'inputParameters';
        return;
      }

      ctrl.state = 'showResult';
      ctrl.call = ctrl.name + '(' + ctrl.arguments.join(', ') + ')';
      ApplicationJmx.invoke(ctrl.parent.application, ctrl.parent.bean, ctrl.signature, ctrl.arguments).then(function (response) {
        ctrl.response = response;
      }).catch(function (response) {
        ctrl.response = response;
      });
    };
  },
  template: require('./jmxInvocation.tpl.html')
};
