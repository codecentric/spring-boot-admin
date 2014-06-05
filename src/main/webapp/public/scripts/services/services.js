'use strict';

angular.module('registry.services', ['ngResource'])
.factory('Application', ['$resource',
  function($resource){
    return $resource(
    		'/api/applications', {}, {
    			query: {method:'GET', isArray:true},
    			create: { method: 'POST' }
    		});
  }])
  .factory('ApplicationInfo', ['$resource',
  function($resource){
    return $resource(
    		':url', 
    		{url:'@url'}, {
    			query: { method: 'GET' },
    			update: {method: 'PUT', params: {id: '@id'} },
    		});
  }]);