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
  				app.info = response;
  			});
  		}
  		this.getMetrics = function(app) {
  			return $http.get(app.url + '/metrics').success(function(response) {
  				app.metrics = response;
  			});
  		}
  	}]);
