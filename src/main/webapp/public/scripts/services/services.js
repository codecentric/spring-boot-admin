'use strict';

angular.module('registry.services', ['ngResource'])
.factory('Application', ['$resource',
  function($resource){
    return $resource(
    		'/api/applications', {}, {
    			query: { method:'GET', isArray:true }
    		});
  }])
  .factory('ApplicationInfo', ['$resource',
  function($resource){
    return $resource(
    		decodeURIComponent(':url'), 
    		{}, {
    			query: { method: 'GET' } ,
    		});
  }]);