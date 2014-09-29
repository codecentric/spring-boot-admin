/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
  	.controller('detailsMetricsCtrl', function ($scope, $stateParams, Application, ApplicationDetails, Abbreviator) {
  		$scope.memoryData = [];
  		$scope.heapMemoryData = [];
  		$scope.counterData = [];
  		$scope.gaugeData = [];
  		
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getMetrics(application, function(application) {
  				//*** Extract data for JVM-Memory-Chart
  				application.metrics["mem.used"] = application.metrics["mem"] - $scope.application.metrics["mem.free"];
  				$scope.memoryData = [ ['Free Memory', application.metrics["mem.free"]],
  				                      ['Used Memory', application.metrics["mem.used"]] ];
  				
  				//*** Extract data for Heap-Memory-Chart
  				application.metrics["heap.free"] = application.metrics["heap"] - $scope.application.metrics["heap.used"];
  				$scope.heapMemoryData = [ ['Free Heap', application.metrics["heap.free"]],
  				                          ['Used Heap', application.metrics["heap.used"]] ];

  				
  				//*** Extract data for Counter-Chart and Gauge-Chart
  				$scope.counterData = [ { key : "value", values: [] } ];
  				$scope.gaugeData = [ { key : "value", values: [] } ];
  				for (var metric in application.metrics) {
  					var matchCounter = /counter\.(.+)/.exec(metric);
  					if ( matchCounter !== null) {
  						$scope.counterData[0].values.push([ matchCounter[1], application.metrics[metric] ]);
  					}

  					var matchGauge = /gauge\.(.+)/.exec(metric);
  					if ( matchGauge !== null) {
  						$scope.gaugeData[0].values.push([ matchGauge[1], application.metrics[metric] ]);
  					}
  				}

  			});
  		});
  		
   		var colorArray = ['#6db33f',  '#a5b2b9', '#34302d' ];
  		$scope.colorFunction = function() {
  			return function(d, i) {
  		    	return colorArray[i % colorArray.length];
  		    };
  		}
  		
		$scope.abbreviateFunction = function(targetLength, preserveLast, shortenThreshold){
  		    return function(s) {
  		        return Abbreviator.abbreviate(s, targetLength, preserveLast, shortenThreshold)
  		    };
  		}

		$scope.intFormatFunction = function(){
  		    return function(d) {
  		        return d3.format('d')(d);
  		    };
  		}
  		
  		$scope.toolTipContentFunction = function(){
  			return function(key, x, y, e, graph) {
  		    	return e.point[0] + ': ' + e.point[1] ;
  			}
  		}
  		
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
