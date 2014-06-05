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
      .when('/add-service', {
        templateUrl: 'views/detail.html',
        controller: 'ServiceCreationCtrl'
      })
      .when('/services/:id', {
        templateUrl: 'views/detail.html',
        controller: 'ServiceDetailCtrl'
      })
      .otherwise({
        redirectTo: '/'
      });
  });