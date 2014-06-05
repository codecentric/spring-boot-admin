'use strict';

angular.module('service-registry')
  .controller('MainCtrl', function ($scope, Application, ApplicationDetail, $location) {
	  
	//Gets the service from /api/services
	$scope.applications = Application.query(); 
	
	$scope.addApplication = function () {		
        window.location = "/#add-applications";
    };
    
    // callback for ng-click 'editService':
    $scope.editApplication = function (id) {
        $location.path('/applications/' + id);
    };

    // callback for ng-click 'deleteService':
    $scope.deleteApplication = function (id) {
    	ApplicationDetail.delete({ id: id });
        $scope.applications = Application.query();
    };
    
  })
  .controller('ApplicationCreationCtrl', function ($scope, Application, $location) {
      // callback for ng-click 'saveService':
      $scope.saveApplication = function () {
          Service.create($scope.service);
          $location.path('/applications');
      }
      // callback for ng-click 'cancel':
      $scope.cancel = function () {
          $location.path('/applications');
      };
  })
  .controller('ApplicationDetailCtrl', function ($scope, $routeParams, Application, ApplicationDetail, $location) {
      // callback for ng-click 'updateService':
      $scope.saveApplication = function () {
    	  ApplicationDetail.update($scope.service);
          $location.path('/applications');
      }
      // callback for ng-click 'cancel':
      $scope.cancel = function () {
          $location.path('/applications');
      };
      $scope.service = ApplicationDetail.show({id: $routeParams.id});
  });