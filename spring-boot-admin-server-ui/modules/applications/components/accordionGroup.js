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

require('./accordionGroup.css');

var id = 0;

module.exports = {
  transclude: {
    heading: 'sbaAccordionHeading',
    body: 'sbaAccordionBody'
  },
  require: {
    accordion: '^sbaAccordion'
  },
  controller: function () {
    var ctrl = this;
    ctrl.$onInit = function () {
      ctrl.id = ctrl.accordion.id + '-' + id++;
    };
  },
  template: require('./accordionGroup.tpl.html')
};
