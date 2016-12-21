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
    application: '<detailsFor'
  },
  controller: function (ApplicationViews) {
    'ngInject';

    var ctrl = this;
    ctrl.primaryView = null;
    ctrl.secondaryViews = [];
    ctrl.resolveViews = null;

    ctrl.$onChanges = function () {
      var result = ApplicationViews.getApplicationViews(ctrl.application);
      ctrl.primaryView = result.views[0];
      ctrl.secondaryViews = result.views.slice(1);
      ctrl.viewsResolved = false;
      ctrl.resolveViews = function () {
        result.resolve();
        ctrl.viewsResolved = true;
      };
    };
  },
  template: require('./btnDetailViews.tpl.html')
};
