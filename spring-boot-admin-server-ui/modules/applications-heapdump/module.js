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

var angular = require('angular');

var module = angular.module('sba-applications-heapdump', ['sba-applications']);
global.sbaModules.push(module.name);

module.run(function ($sce, $http, ApplicationViews) {

  ApplicationViews.register({
    order: 110,
    title: $sce.trustAsHtml('<i class="fa fa-cubes fa-fw"></i>Heapdump'),
    href: 'api/applications/{id}/heapdump',
    target: '_blank',
    show: function (application) {
      return $http({ method: 'OPTIONS', url: 'api/applications/' + application.id + '/heapdump' }).then(function (response) {
        return response.headers('Allow') === 'GET,HEAD'; //Test the exact headers, in case the DispatcherServlet responses to the request for older boot-versions
      }).catch(function () {
        return false;
      });
    }
  });

});
