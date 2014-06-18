'use strict';

angular.module('springBootAdmin')
  .controller('overviewCtrl', function ($scope, Applications, ApplicationInfo, $location, $http) {
	$scope.applications = Applications.query({}, function(applications) {
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
  .controller('infosCtrl', function ($scope, Application, ApplicationInfo) {
	$scope.application = Application.query({}, function(application) {
	  ApplicationInfo.getInfo(application);
	});
  });