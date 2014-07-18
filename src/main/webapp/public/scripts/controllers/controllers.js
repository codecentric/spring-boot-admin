'use strict';

angular.module('springBootAdmin')
  	.controller('overviewCtrl', function ($scope, Applications, Application, ApplicationOverview, $location, $interval) {
  		$scope.loadData = function() {
  			$scope.applications = Applications.query({}, function(applications) {
	  			for (var i = 0; i < applications.length; i++) {
	  				var app = applications[i];
	  				ApplicationOverview.getVersion(app);
	  				ApplicationOverview.getHealth(app);
	  				ApplicationOverview.getLogfile(app);
	  			}	
	  		});
  		}
  		$scope.loadData();
  		// callback for ng-click 'showDetails':
  		$scope.showDetails = function(id) {
  			$location.path('/apps/details/' + id + '/infos');
  		};
  		// callback for ng-click 'showLogging':
  		$scope.showLogging = function(id) {
  			$location.path('/apps/logging/' + id + '/read');
  		};
  		// callback for ng-click 'refresh':
  		$scope.refresh = function(id) {
  			$scope.application = Application.query({id: id}, function(application) {
  				ApplicationOverview.refresh(application);
  	  		});
  		};
  		// reload site every 30 seconds
  		var task = $interval(function() {
  			$scope.loadData();
  		}, 30000);
  	})
  	.controller('navCtrl', function ($scope, $location) {
  		$scope.navClass = function(page) {
  			var currentRoute = $location.path().substring(1) || 'main';
  			return page == currentRoute ? 'active' : '';
  		};
  	})
  	.controller('detailsCtrl', function ($scope, $stateParams, Application) {
  		$scope.application = Application.query({id: $stateParams.id});
  	})
  	.controller('detailsInfosCtrl', function ($scope, $stateParams, Application, ApplicationDetails) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getInfo(application);
  		});
  	})
  	.controller('detailsMetricsCtrl', function ($scope, $stateParams, Application, ApplicationDetails) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getMetrics(application);
  		});
  	})
  	.controller('detailsEnvCtrl', function ($scope, $stateParams, Application, ApplicationDetails) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getEnv(application);
  		});
  	})
  	.controller('detailsPropsCtrl', function ($scope, $stateParams, Application, ApplicationDetails) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getProps(application);
  		});
  	})
  	.controller('loggingCtrl', function ($scope, $stateParams, Application) {
  		$scope.application = Application.query({id: $stateParams.id});
  	})
  	.controller('loggingReadCtrl', function ($scope, $stateParams, Application, ApplicationLogging) {
  		$scope.$parent.application.logger = new Object(); 
  		$scope.readLoglevel = function(application) {
  			ApplicationLogging.getLoglevel(application);
  		};
  	})
  	.controller('loggingWriteCtrl', function ($scope, $stateParams, Application, ApplicationLogging) {
  		$scope.$parent.application.logger = new Object(); 
  		$scope.writeLoglevel = function(application) {
  			ApplicationLogging.setLoglevel(application);
  		};
  	});
