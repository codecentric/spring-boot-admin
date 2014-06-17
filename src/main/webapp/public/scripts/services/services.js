'use strict';

angular.module('registry.services', ['ngResource'])
.factory('Application', ['$resource',
  function($resource){
    return $resource(
    		'/api/applications', {}, {
    			query: { method:'GET', isArray:true }
    		});
  }])
  .service('ApplicationInfo', ['$http',
  function($http){
	this.getInfo = function(app) {
    	return $http.get(app.url + '/info').success(function(response) {
    		app.version = response.version;
    	});
    }
  }]);