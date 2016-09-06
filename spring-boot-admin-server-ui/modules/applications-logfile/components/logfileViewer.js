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

require('./logfileViewer.css');

module.exports = {
  bindings: {
    url: '@logfileUrl',
    interval: '@?refreshInterval',
    initialSize: '@?initialSize',
    onAppend: '&onAppend'
  },
  controller: function ($timeout, Logtail, $element, $document, $filter) {
    'ngInject';
    var ctrl = this;

    ctrl.$onInit = function () {
      ctrl.interval = ctrl.interval || 1000;
      ctrl.initialSize = ctrl.initialSize || 300 * 1024;
      ctrl.logtail = new Logtail(ctrl.url, ctrl.initialSize);
      ctrl.totalSize = 0;
      ctrl.currentSize = 0;
      var logview = $element.children('.logview');
      var doRefresh = function () {
        ctrl.logtail.refresh().then(function (chunk) {
          ctrl.totalSize = chunk.totalSize;
          if (chunk.first) {
            logview.empty();
            var skippedBytes = Math.max(chunk.totalSize - ctrl.initialSize, 0);
            if (skippedBytes > 0) {
              logview.append($document[0].createTextNode('... first ' + $filter('humanBytes')(skippedBytes) + ' skipped ...\n\n'));
            }
          }
          if (chunk.addendum !== null) {
            logview.append($document[0].createTextNode(chunk.addendum));
            if (ctrl.onAppend) {
              ctrl.onAppend();
            }
          }
          ctrl.promise = $timeout(doRefresh, ctrl.interval);
        }).catch(function (response) {
          ctrl.error = response;
        });
      };
      doRefresh();
    };

    ctrl.$onDestroy = function () {
      if (ctrl.promise) {
        $timeout.cancel(ctrl.promise);
      }
    };
  },
  template: require('./logfileViewer.tpl.html')
};
