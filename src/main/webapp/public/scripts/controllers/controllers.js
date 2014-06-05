'use strict';

angular.module('service-registry')
  .controller('MainCtrl', function ($scope, Service, ServiceDetail, $location) {
	  
	//Gets the service from /api/services
	$scope.services = Service.query(); 
	
	$scope.addService = function () {		
        window.location = "/#add-service";
    };
    
    // callback for ng-click 'editService':
    $scope.editService = function (id) {
        $location.path('/services/' + id);
    };

    // callback for ng-click 'deleteService':
    $scope.deleteService = function (id) {
    	ServiceDetail.delete({ id: id });
        $scope.services = Service.query();
    };
    
  })
  .controller('ServiceCreationCtrl', function ($scope, Service, $location) {
      // callback for ng-click 'saveService':
      $scope.saveService = function () {
          Service.create($scope.service);
          $location.path('/services');
      }
      // callback for ng-click 'cancel':
      $scope.cancel = function () {
          $location.path('/services');
      };
  })
  .controller('ServiceDetailCtrl', function ($scope, $routeParams, Service, ServiceDetail, $location) {
      // callback for ng-click 'updateService':
      $scope.saveService = function () {
    	  ServiceDetail.update($scope.service);
          $location.path('/services');
      }
      // callback for ng-click 'cancel':
      $scope.cancel = function () {
          $location.path('/services');
      };
      $scope.service = ServiceDetail.show({id: $routeParams.id});
  });