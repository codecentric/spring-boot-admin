'use strict';

angular.module('springBootAdmin.services', ['ngResource'])
  	.factory('Applications', ['$resource', function($resource) {
  		return $resource(
  				'/api/applications', {}, {
  					query: { method:'GET', isArray:true }
  				});
  		}
  	])
  	.factory('Application', ['$resource', function($resource) {
  		return $resource(
  				'/api/application/:id', {}, {
  					query: { method:'GET'}
  				});
  		}
  	])
  	.service('ApplicationOverview', ['$http', function($http) {
  		this.getVersion = function(app) {
  			return $http.get(app.url + '/info').success(function(response) {
  				app.version = response.version;
  			}).error(function() {
  				app.version = '---';
  			});
  		}
  		this.getHealth = function(app) {
  			return $http.get(app.url + '/health').success(function(response) {
  				if (response.indexOf('ok') != -1 || response.status.indexOf('ok') != -1) { 
  					app.status = 'online';
  				} else {
  					app.status = 'offline';
  				}
  			}).error(function() {
  				app.status = 'offline';
  			});
  		}
  	}])
  	.service('ApplicationDetails', ['$http', function($http) {
  		this.getInfo = function(app) {
  			return $http.get(app.url + '/info').success(function(response) {
  				app.info = angular.toJson(response, true);
  			});
  		}
  		this.getMetrics = function(app) {
  			return $http.get(app.url + '/metrics').success(function(response) {
  				app.metrics = response;
  			});
  		}
  		this.getEnv = function(app) {
  			return $http.get(app.url + '/env').success(function(response) {
  				app.env = response;
  				app.env.systemProp = angular.toJson(app.env['systemProperties'], true);
  				app.env.systemEnv = angular.toJson(app.env['systemEnvironment'], true);
  				//app.env.config = response['applicationConfig: [classpath:/application.properties]'];
  			});
  		}
  	}]);
