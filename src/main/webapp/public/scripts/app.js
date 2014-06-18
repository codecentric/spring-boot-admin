'use strict';

angular.module('springBootAdmin', [
  'ngResource',
  'ngRoute',
  'springBootAdmin.services'
])
  .config(function ($routeProvider) {
    $routeProvider
      .when('/main', {
        templateUrl: 'views/main.html',
        controller: 'mainCtrl'
      })
      .when('/about', {
        templateUrl: 'views/about.html'
      })
      .when('/', {
        redirectTo: '/main'
      })
      .otherwise({
        redirectTo: '/'
      });
  });