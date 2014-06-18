'use strict';

angular.module('springBootAdmin')
  .controller('overviewCtrl', function ($scope, Application, ApplicationInfo, $location, $http) {
	// Gets the service from /api/services
	$scope.applications = Application.query({}, function(applications) {

		// Get details from applications
		for (var i = 0; i < applications.length; i++) {
			var app = applications[i];
			ApplicationInfo.getInfo(app);
			ApplicationInfo.getHealth(app);
		}	
	});
	
  })
  .controller('navCtrl', function ($scope, $location) {
	$scope.navClass = function(page) {
	  var currentRoute = $location.path().substring(1) || 'main';
	  return page == currentRoute ? 'active' : '';
	};
  })
  .controller('metricsCtrl', function ($scope, $location) {
	 
  });