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

require('./popover.css');

module.exports = {
  transclude: true,
  bindings: {
    title: '@popoverTitle',
    toggle: '<popoverToggle'
  },
  controllerAs: '$popover',
  controller: function ($element) {
    'ngInject';
    var ctrl = this;
    ctrl.offset = null;

    ctrl.$onChanges = function (changes) {
      if (changes.toggle.currentValue) {
        ctrl.offset = $element.parent().offset();
        ctrl.offset.left -= (($element.children().first().outerWidth() - $element.parent().outerWidth()) / 2);
        ctrl.offset.top += + (($element.parent().outerHeight()));
      }
    };

    ctrl.isVisible = function () {
      return ctrl.toggle;
    };

  },
  template: require('./popover.tpl.html')
};
