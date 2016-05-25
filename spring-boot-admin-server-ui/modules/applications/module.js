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

var module = angular.module('sba-applications', ['sba-core', require('angular-resource')]);
global.sbaModules.push(module.name);

module.controller('applicationsCtrl', require('./controllers/applicationsCtrl.js'));
module.controller('applicationsHeaderCtrl', require('./controllers/applicationsHeaderCtrl.js'));

module.service('Application', require('./services/application.js'));
module.service('Notification', require('./services/notification.js'));
module.service('NotificationFilters', require('./services/notificationFilters.js'));
module.service('ApplicationViews', require('./services/applicationViews.js'));

module.filter('yaml', require('./filters/yaml.js'));
module.filter('limitLines', require('./filters/limitLines.js'));

module.component('sbaInfoPanel', require('./components/infoPanel.js'));
module.component('sbaAccordion', require('./components/accordion.js'));
module.component('sbaAccordionGroup', require('./components/accordionGroup.js'));
module.component('sbaNotificationFilter', require('./components/notificationFilter.js'));

require('./css/module.css');

module.config(function ($stateProvider) {
  $stateProvider.state('applications-list', {
    url: '/',
    templateUrl: 'applications/views/applications-list.html',
    controller: 'applicationsCtrl'
  }).state('applications', {
    abstract: true,
    url: '/applications/:id',
    controller: 'applicationsHeaderCtrl',
    templateUrl: 'applications/views/application-nav.html',
    resolve: {
      application: function ($stateParams, Application) {
        return Application.get({
          id: $stateParams.id
        }).$promise;
      }
    }
  });
});

module.run(function ($rootScope, $state, $filter, Notification, Application, MainViews) {
  MainViews.register({
    title: 'Applications',
    state: 'applications-list',
    order: -100
  });

  $rootScope.applications = [];

  $rootScope.indexOfApplication = function (id) {
    for (var i = 0; i < $rootScope.applications.length; i++) {
      if ($rootScope.applications[i].id === id) {
        return i;
      }
    }
    return -1;
  };

  var refresh = function (application) {
    application.info = {};
    application.refreshing = true;
    application.getCapabilities();
    application.getInfo().then(function (response) {
      var info = response.data;
      application.version = info.version;
      application.infoDetails = null;
      application.infoShort = '';
      delete info.version;
      var infoYml = $filter('yaml')(info);
      if (infoYml !== '{}\n') {
        application.infoShort = $filter('limitLines')(infoYml, 3);
        if (application.infoShort !== infoYml) {
          application.infoDetails = $filter('limitLines')(infoYml, 32000, 3);
        }
      }
    }).finally(function () {
      application.refreshing = false;
    });
  };

  Application.query(function (applications) {
    for (var i = 0; i < applications.length; i++) {
      refresh(applications[i]);
      $rootScope.applications.push(applications[i]);
    }
  });

  // setups up the sse-reciever
  var journalEventSource = new EventSource('api/journal?stream');
  journalEventSource.onmessage = function (message) {
    var event = JSON.parse(message.data);
    Object.setPrototypeOf(event.application, Application.prototype);

    var options = {
      tag: event.application.id,
      body: 'Instance ' + event.application.id + '\n' + event.application.healthUrl,
      icon: require('./img/unknown.png'),
      timeout: 10000,
      url: $state.href('apps.details', {
        id: event.application.id
      })
    };
    var title = event.application.name;
    var index = $rootScope.indexOfApplication(event.application.id);

    if (event.type === 'REGISTRATION') {
      if (index === -1) {
        $rootScope.applications.push(event.application);
      }

      title += ' instance registered.';
      options.tag = event.application.id + '-REGISTRY';
    } else if (event.type === 'DEREGISTRATION') {
      if (index > -1) {
        $rootScope.applications.splice(index, 1);
      }

      title += ' instance removed.';
      options.tag = event.application.id + '-REGISTRY';
    } else if (event.type === 'STATUS_CHANGE') {
      refresh(event.application);
      if (index > -1) {
        $rootScope.applications[index] = event.application;
      } else {
        $rootScope.applications.push(event.application);
      }

      title += ' instance is ' + event.to.status;
      options.tag = event.application.id + '-STATUS';
      options.icon = event.to.status !== 'UP' ? require('./img/error.png') : require('./img/ok.png');
      options.body = event.from.status + ' --> ' + event.to.status + '\n' + options.body;
    }

    $rootScope.$apply();
    Notification.notify(title, options);
  };

});
