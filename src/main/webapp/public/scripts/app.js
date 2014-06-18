'use strict';

angular.module('springBootAdmin', [
  'ngResource',
  'ngRoute',
  'ui.router',
  'springBootAdmin.services'
])
  .config(function ($stateProvider, $urlRouterProvider) {
	$urlRouterProvider
	  .when('/', '/apps/overview')
	  .otherwise('/')
	$stateProvider
	  .state('apps', {
		url: '/apps',
		templateUrl: 'views/apps.html',
	  })
	  .state('about', {
		url: '/about',
		templateUrl: 'views/about.html'
	  })
	  .state('apps.overview', {
		url: '/overview',
		templateUrl: 'views/apps/overview.html',
	    controller: 'overviewCtrl'
	  })
	  .state('apps.details', {
		url: '/details',
		templateUrl: 'views/apps/details.html'
	  })
	  .state('apps.details.infos', {
		url: '/infos/{id}',
		templateUrl: 'views/apps/details/infos.html',
		controller: 'infosCtrl'
	  })
	  .state('apps.details.metrics', {
		url: '/metrics/{id}',
		templateUrl: 'views/apps/details/metrics.html'
	  });
  })
  .run(function ($rootScope, $state, $stateParams, $log) {
    $rootScope.$state = $state;
    $rootScope.$stateParams = $stateParams;
    $rootScope.springBootAdminServerUrl = window.location.protocol + '//' + window.location.host;
  });
