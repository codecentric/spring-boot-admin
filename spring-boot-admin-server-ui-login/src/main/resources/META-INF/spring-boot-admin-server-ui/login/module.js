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

(function (sbaModules, angular) {
  'use strict';
  var module = angular.module('sba-login', ['sba-core']);
  sbaModules.push(module.name);

  module.controller('logoutCtrl', ['$scope', '$document', function () {
    angular.element('#logout-form').submit();
  }]);

  module.config(['$stateProvider', function ($stateProvider) {
    $stateProvider.state('logout', {
      template: '<form id="logout-form" action="logout" method="post"></form>',
      name: 'logout',
      url: '/logout',
      controller: 'logoutCtrl'
    });
  }]);

  module.run(['MainViews', '$sce', function (MainViews, $sce) {
    MainViews.register({
      title: $sce.trustAsHtml('<i class="fa fa-2x fa-sign-out" aria-hidden="true"></i>'),
      state: 'logout',
      order: 9999
    });
  }]);
}(sbaModules, angular));
