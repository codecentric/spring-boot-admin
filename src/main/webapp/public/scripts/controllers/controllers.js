'use strict';

angular.module('registry')
  .controller('MainCtrl', function ($scope, Application, ApplicationInfo, $location, $http) {
	  
	// Gets the service from /api/services
	$scope.applications = Application.query({}, function(applications) {

		// Get details from applications
		for (var i = 0; i < applications.length; i++) {
			var app = applications[i];
			ApplicationInfo.getInfo(app);
			ApplicationInfo.getHealth(app);
		}	
	});
	
  });