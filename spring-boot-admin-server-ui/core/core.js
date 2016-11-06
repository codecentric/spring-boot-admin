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

require('bootstrap');
require('bootstrap-responsive.css');
require('bootstrap.css');

require('font-awesome/css/font-awesome.css');
require('googlefonts.css');
require('./css/core.css');

require('./img/favicon.png');

require('eventsource-polyfill');
require('es6-shim');

var angular = require('angular');
var module = angular.module('sba-core', [require('angular-ui-router')]);
global.sbaModules.push(module.name);

module.config(function ($urlRouterProvider) {
  $urlRouterProvider.otherwise('/');
});

module.service('MainViews', function () {
  var views = [];
  this.register = function (view) {
    views.push(view);
    views.sort(function (v1, v2) {
      return (v1.order || 0) - (v2.order || 0);
    });
  };
  this.getViews = function () {
    return views;
  };
});

module.run(function ($rootScope, $state, MainViews) {
  $rootScope.$state = $state;
  $rootScope.mainViews = MainViews.getViews();
  $rootScope.modules = global.sbaModules;
});
