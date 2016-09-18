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

require('./limitedText.css');

module.exports = {
  bindings: {
    maxLines: '@maxLines',
    content: '<bindHtml'
  },
  controller: function () {
    'ngInject';
    var ctrl = this;
    ctrl.limited = '';
    ctrl.collapsed = true;

    var limit = function (text, limit) {
      var lines = text.split('\n');
      if (lines.length > limit) {
        return [lines.slice(0, limit).join('\n'), lines.slice(limit).join('\n')];
      }
      return [text];
    };

    ctrl.$onChanges = function () {
      ctrl.parts = limit(ctrl.content, ctrl.maxLines);
      ctrl.isLimited = ctrl.parts.length > 1;
    };

    ctrl.toggle = function () {
      ctrl.collapsed = !ctrl.collapsed;
      ctrl.$onChanges();
    };

  },
  template: '<div style="white-space: pre;"><ng-bind-html ng-bind-html="$ctrl.parts[0]"></ng-bind-html> <span class="ellipsis-expander" ng-show="$ctrl.isLimited" ng-click="$ctrl.toggle()">...</span><ng-bind-html ng-bind-html="$ctrl.collapsed ? \'\' : \'\n\' + $ctrl.parts[1]"></ng-bind-html></div>'
};
