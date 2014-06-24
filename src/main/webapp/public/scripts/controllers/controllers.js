'use strict';

angular.module('springBootAdmin')
  .controller('overviewCtrl', function ($scope, Applications, ApplicationInfo, $location) {
	$scope.applications = Applications.query({}, function(applications) {
	  for (var i = 0; i < applications.length; i++) {
		var app = applications[i];
		ApplicationInfo.getInfo(app);
		ApplicationInfo.getHealth(app);
	  }	
	});
	// callback for ng-click 'showDetails':
    $scope.showDetails = function(id) {
        $location.path('/apps/details/infos/' + id);
    };
  })
  .controller('navCtrl', function ($scope, $location) {
	$scope.navClass = function(page) {
	  var currentRoute = $location.path().substring(1) || 'main';
	  return page == currentRoute ? 'active' : '';
	};
  })
  .controller('detailsCtrl', function ($scope, $stateParams, Application, ApplicationInfo) {
	$scope.application = Application.query({id: $stateParams.id}, function(application) {
		// nop
	});
  })
  .controller('infosCtrl', function ($scope, $stateParams, Application, ApplicationInfo) {
	$scope.application = Application.query({id: $stateParams.id}, function(application) {
	  //ApplicationInfo.getInfo(application);
	});
  })
  .controller('metricsCtrl', function ($scope, $stateParams, Application, ApplicationInfo) {
	$scope.application = Application.query({id: $stateParams.id}, function(application) {
	  //ApplicationInfo.getInfo(application);
	});
  });