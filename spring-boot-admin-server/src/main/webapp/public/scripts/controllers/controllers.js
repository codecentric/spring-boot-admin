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
  		// callback for ng-click 'showJmx':
  		$scope.showJmx = function(id) {
  			$location.path('/apps/jmx/' + id);
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
  				$scope.gaugeData = [ { key : "value", values: []   }, 
  				                     { key : "average", values: [] },
  									 { key : "min", values: []     },
  									 { key : "max", values: []     },
  				                     { key : "count", values: []   } ];
  			
  				for (var metric in application.metrics) {
  					var matchCounter = /counter\.(.+)/.exec(metric);
  					if ( matchCounter !== null) {
  						$scope.counterData[0].values.push([ matchCounter[1], application.metrics[metric] ]);
  						continue;
  					}

  					var matchGaugeValue = /gauge\.(.+)\.val/.exec(metric);
  					if ( matchGaugeValue !== null) {
  						$scope.gaugeData[0].values.push([ matchGaugeValue[1], application.metrics[metric] ]);
  						continue;
  					}
  					
  					var matchGaugeAvg = /gauge\.(.+)\.avg/.exec(metric);
  					if ( matchGaugeAvg !== null) {
  						$scope.gaugeData[1].values.push([ matchGaugeAvg[1], application.metrics[metric] ]);
  						continue;
  					}
  					
  					var matchGaugeMin = /gauge\.(.+)\.min/.exec(metric);
  					if ( matchGaugeMin !== null) {
  						$scope.gaugeData[2].values.push([ matchGaugeMin[1], application.metrics[metric] ]);
  						continue;
  					}
  					
  					var matchGaugeMax = /gauge\.(.+)\.max/.exec(metric);
  					if ( matchGaugeMax !== null) {
  						$scope.gaugeData[3].values.push([ matchGaugeMax[1], application.metrics[metric] ]);
  						continue;
  					}
  					
  					var matchGaugeCount = /gauge\.(.+)\.count/.exec(metric);
  					if ( matchGaugeCount !== null) {
  						$scope.gaugeData[4].values.push([ matchGaugeCount[1], application.metrics[metric] ]);
  						continue;
  					}
  					
  					var matchGaugeAlpha = /gauge\.(.+)\.alpha/.exec(metric);
  					if ( matchGaugeAlpha !== null) {
  						continue;
  					} 
  					
  					var matchGaugeValue = /gauge\.(.+)/.exec(metric);
  					if ( matchGaugeValue !== null) {
  						$scope.gaugeData[0].values.push([ matchGaugeValue[1], application.metrics[metric] ]);
  					}
  				}
  				
  				//in case no richGauges are present remove empty groups
  				var i = $scope.gaugeData.length;
  				while (--i) {
  					if ($scope.gaugeData[i].values.length === 0) {
  						$scope.gaugeData.splice(i, 1);
  					}
  				}

  			});
  		});
  		
   		var colorArray = ['#6db33f', '#a5b2b9', '#34302d'  , '#fec600' ,'#4e681e' ];
  		$scope.colorFunction = function() {
  			return function(d, i) {
  		    	return colorArray[i % colorArray.length];
  		    };
  		}
  		
		$scope.abbreviateFunction = function(targetLength, preserveLast, shortenThreshold){
  		    return function(s) {
  		        return Abbreviator.abbreviate(s, '.', targetLength, preserveLast, shortenThreshold)
  		    };
  		}

		$scope.intFormatFunction = function(){
  		    return function(d) {
  		        return d3.format('d')(d);
  		    };
  		}
  		
  		$scope.toolTipContentFunction = function(){
  			return function(key, x, y, e, graph) {
  		    	return '<b>' + key + '</b> ' +e.point[0] + ': ' + e.point[1] ;
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
  	.controller('detailsClasspathCtrl', function ($scope, $stateParams, Application, ApplicationDetails, Abbreviator) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getClasspath(application);
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
  	})
  	.controller('jmxCtrl', function ($scope, $stateParams, $modal, Application, ApplicationJMX) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			$scope.error = null;
  			$scope.domains = [];
  			ApplicationJMX.list(application).then(function(domains){
  				$scope.domains = domains;
  			}).catch(function(response) {
  				$scope.error = response.error;
  				console.log(response.stacktrace);  				
  			})
  		});
  		
  		$scope.readAttr = function(bean) {
  			bean.error = null;
  			ApplicationJMX.readAttr($scope.application, bean).then(
  					function(response) {
		  				for (var name in response.value) {
		  					bean.attributes[name].error = null;
		  					bean.attributes[name].value = response.value[name];
		  					bean.attributes[name].jsonValue = JSON.stringify(response.value[name], null, "   ");
		  				}
		  			}).catch(function(response) {
		  				bean.error = response.error;
		  				console.log(response.stacktrace);
		  			});
  		}
  		
  		$scope.writeAttr = function(bean, name, attr) {
  			attr.error = null;
  			ApplicationJMX.writeAttr($scope.application, bean, name, attr.value).catch(
  					function(response) {
  						attr.error = response.error;
  						console.log(response.stacktrace);
  					});
  		}
  		
  		$scope.invoke = function() {
  			
  			$scope.invocation.state = 'executing';
  			
  			ApplicationJMX.invoke($scope.application, $scope.invocation.bean, $scope.invocation.opname, $scope.invocation.args).then(
  					function(response) {
  						$scope.invocation.state = 'success';
  						$scope.invocation.result = response.value;
  					}).catch(function(response){
  						$scope.invocation.state = 'error';
  						$scope.invocation.error = response.error;
  						$scope.invocation.stacktrace = response.stacktrace;
  					});
  			
  			$modal.open({
  				templateUrl: 'invocationResultDialog.html',
  				scope: $scope
  				}).result.then(function() {
  					$scope.invocation = null;  		
				});
  		}
  		
  		$scope.prepareInvoke = function(bean, name, op) {
  			$scope.invocation = { bean: bean, opname: name, opdesc: op, args: [], state: 'prepare' };

  			if (op instanceof Array) {
  				$modal.open({
				      templateUrl: 'invocationVariantDialog.html',
				      scope: $scope
				    }).result.then(function(chosenOp) {
						$scope.prepareInvoke(bean, name, chosenOp);
					}).catch(function() {
			  			$scope.invocation = null;  						
					});
  			} else {
  				if (op.args.length === 0) {
  					$scope.invoke();
  				} else {
  					var signature = "(";
  					for (var i in op.args) {
  						if (i > 0) signature += ',';
  						signature += op.args[i].type;
  						$scope.invocation.args[i] = null;
  					}
  					signature += ")";
  					$scope.invocation.opname = name + signature;
  				    
  					$modal.open({
  				      templateUrl: 'invocationPrepareDialog.html',
  				      scope: $scope
  				    }).result.then(function() {
  						$scope.invoke();
  					}).catch(function() {
  			  			$scope.invocation = null;  						
  					});
  				}
  			}
  		}
  	});
