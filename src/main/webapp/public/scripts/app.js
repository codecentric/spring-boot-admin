'use strict';

angular.module('service-registry', [
  'ngCookies',
  'ngResource',
  'ngSanitize',
  'ngRoute',
  'ngResource',
  'service-registry.services'
])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl'
      })
      .when('/add-application', {
        templateUrl: 'views/detail.html',
        controller: 'ApplicationCreationCtrl'
      })
      .when('/applications/:id', {
        templateUrl: 'views/detail.html',
        controller: 'ApplicationDetailCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  });