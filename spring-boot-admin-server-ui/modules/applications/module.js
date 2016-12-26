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

var module = angular.module('sba-applications', ['sba-core', require('angular-resource'), require('angular-sanitize')]);
global.sbaModules.push(module.name);

module.controller('applicationsCtrl', require('./controllers/applicationsCtrl.js'));
module.controller('applicationsHeaderCtrl', require('./controllers/applicationsHeaderCtrl.js'));

module.service('Application', require('./services/application.js'));
module.service('ApplicationGroups', require('./services/applicationGroups.js'));
module.service('Notification', require('./services/notification.js'));
module.service('NotificationFilters', require('./services/notificationFilters.js'));
module.service('ApplicationViews', require('./services/applicationViews.js'));

module.filter('yaml', require('./filters/yaml.js'));
module.filter('linkify', require('./filters/linkify.js'));

module.component('sbaInfoPanel', require('./components/infoPanel.js'));
module.component('sbaAccordion', require('./components/accordion.js'));
module.component('sbaAccordionGroup', require('./components/accordionGroup.js'));
module.component('sbaNotificationSettings', require('./components/notificationSettings.js'));
module.component('sbaPopover', require('./components/popover.js'));
module.component('sbaLimitedText', require('./components/limitedText.js'));
module.component('sbaStatusInfo', require('./components/statusInfo.js'));
module.component('sbaBtnDetailViews', require('./components/btnDetailViews.js'));

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

module.run(function ($rootScope, $state, Notification, Application, ApplicationGroups, MainViews) {
  MainViews.register({
    title: 'Applications',
    state: 'applications-list',
    order: -100
  });

  var applicationGroups = new ApplicationGroups();
  $rootScope.applicationGroups = applicationGroups;

  Application.query(function (applications) {
    applications.forEach(function (application) {
      applicationGroups.addApplication(application, true);
    });
  });

  if (typeof (EventSource) !== 'undefined') {
    // setups up the sse-reciever
    $rootScope.journalEventSource = new EventSource('api/journal?stream');
    $rootScope.$on('$destroy', function () {
      $rootScope.journalEventSource.close();
    });

    $rootScope.journalEventSource.addEventListener('message', function (message) {
      var event = JSON.parse(message.data);
      Object.setPrototypeOf(event.application, Application.prototype);

      var title = event.application.name;
      var options = {
        tag: event.application.id,
        body: 'Instance ' + event.application.id + '\n' + event.application.healthUrl,
        icon: require('./img/unknown.png'),
        timeout: 10000,
        url: $state.href('applications.details', {
          id: event.application.id
        })
      };

      if (event.type === 'REGISTRATION') {
        applicationGroups.addApplication(event.application, false);
        title += ' instance registered.';
        options.tag = event.application.id + '-REGISTRY';
      } else if (event.type === 'DEREGISTRATION') {
        applicationGroups.removeApplication(event.application.id);
        title += ' instance removed.';
        options.tag = event.application.id + '-REGISTRY';
      } else if (event.type === 'STATUS_CHANGE') {
        applicationGroups.addApplication(event.application, true);
        title += ' instance is ' + event.to.status;
        options.tag = event.application.id + '-STATUS';
        options.icon = event.to.status !== 'UP' ? require('./img/error.png') : require('./img/ok.png');
        options.body = event.from.status + ' --> ' + event.to.status + '\n' + options.body;
      }

      $rootScope.$apply();
      Notification.notify(title, options);
    });
  }
});
