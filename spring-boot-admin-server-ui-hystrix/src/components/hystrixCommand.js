/*
 * Copyright 2016 the original author or authors.
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

var id = 0;

require('./hystrix.css');
require('hystrix/hystrixCommand.css');
var HystrixCommandMonitor = require('hystrix/hystrixCommand');

module.exports = {
  bindings: {
    source: '<eventSource'
  },
  controller: function ($element) {
    'ngInject';
    var ctrl = this;
    ctrl.id = id++;

    var startMonitor = function (id) {
      $element.find('#hystrix-command-' + id).html('<span class="loading">Loading ...</span>');
      if (ctrl.source !== null) {
        ctrl.monitor = new HystrixCommandMonitor(0, 'hystrix-command-' + id, { includeDetailIcon: false });
        ctrl.monitor.sortByErrorThenVolume();
        ctrl.source.addEventListener('message', ctrl.monitor.eventSourceMessageListener, false);
      }
    };

    ctrl.$onInit = function () {
      startMonitor(ctrl.id);
    };

    ctrl.$onChanges = function (changes) {
      if (changes.source.currentValue !== changes.source.previousValue) {
        startMonitor(ctrl.id);
      }
    };
  },
  template: require('./hystrixCommand.tpl.html')
};
