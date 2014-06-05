'use strict';

angular.module('registry')
  .controller('MainCtrl', function ($scope, Application, ApplicationInfo, $location) {
	  
	// Gets the service from /api/services
	$scope.applications = Application.query({}, function(applications) {

		// Get details from applications
		for (var i = 0; i < applications.length; i++) {
			var app = applications[i];
			app.version = ApplicationInfo.query({url: app.url}).version;
		}	
	});
	
  });