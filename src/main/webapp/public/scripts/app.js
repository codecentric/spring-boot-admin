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
  				abstract: true,
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
  				url: '/details/:id',
  				abstract: true,
  				templateUrl: 'views/apps/details.html',
  				controller: 'detailsCtrl'
  			})
  			.state('apps.details.infos', {
  				url: '/infos',
  				templateUrl: 'views/apps/details/infos.html',
  				controller: 'detailsInfosCtrl'
  			})
  			.state('apps.details.metrics', {
  				url: '/metrics',
  				templateUrl: 'views/apps/details/metrics.html',
  				controller: 'detailsMetricsCtrl'
  			})
  			.state('apps.details.env', {
  				url: '/env',
  				templateUrl: 'views/apps/details/env.html',
  				controller: 'detailsEnvCtrl'
  			})
  			.state('apps.details.props', {
  				url: '/props',
  				templateUrl: 'views/apps/details/props.html',
  				controller: 'detailsPropsCtrl'
  			})
  			.state('apps.logging', {
  				url: '/logging/:id',
  				abstract: true,
  				templateUrl: 'views/apps/logging.html',
  				controller: 'detailsCtrl'
  			})
  			.state('apps.logging.read', {
  				url: '/read',
  				templateUrl: 'views/apps/logging/read.html',
  				controller: 'loggingReadCtrl'
  			})
  			.state('apps.logging.write', {
  				url: '/write',
  				templateUrl: 'views/apps/logging/write.html',
  				controller: 'loggingWriteCtrl'
  			});
  	})
  	.run(function ($rootScope, $state, $stateParams, $log) {
  		$rootScope.$state = $state;
  		$rootScope.$stateParams = $stateParams;
  		$rootScope.springBootAdminServerUrl = window.location.protocol + '//' + window.location.host;
  	});
