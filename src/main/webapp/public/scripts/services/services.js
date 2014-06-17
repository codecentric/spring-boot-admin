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
	var _app;
	this.setApp = function(app) {
		_app = app;
	}
	this.getInfo = function() {
    	return $http.get(_app.url + '/info').success(function(response) {
    		_app.version = response.version;
    	});
    }
  }]);