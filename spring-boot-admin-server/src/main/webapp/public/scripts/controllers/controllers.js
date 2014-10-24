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
  	.controller('overviewCtrl', ['$scope', '$location', '$interval', 'Applications', 'ApplicationOverview', 
  	                             function ($scope, $location, $interval, Applications, ApplicationOverview) {
  		$scope.loadData = function() {
  			$scope.applications = Applications.query({}, function(applications) {
	  			for (var i in applications) {
	  				var app = applications[i];
	  				ApplicationOverview.getVersion(app);
	  				ApplicationOverview.getHealth(app);
	  				ApplicationOverview.getLogfile(app);
	  			}	
	  		});
  		}
  		$scope.loadData();

  		$scope.refresh = function(id) {
  			$scope.application = Application.query({id: id}, function(application) {
  				ApplicationOverview.refresh(application);
  	  		});
  		};
  		
  		// reload site every 30 seconds
  		var task = $interval(function() {
  			$scope.loadData();
  		}, 30000);
  	}])
  	.controller('navCtrl', ['$scope', '$location', function ($scope, $location) {
  	}])
  	.controller('appsCtrl',  ['$scope', '$stateParams', function ($scope,  $stateParams) {
  		$scope.applicationId = $stateParams.id;
  	}])  	
  	.controller('detailsCtrl', ['$scope', '$stateParams', '$interval', 'Application', 'ApplicationDetails', 
  	                            function ($scope, $stateParams, $interval, Application, ApplicationDetails) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getInfo(application);
  			
  			ApplicationDetails.getMetrics(application, function(application){
  				application.metrics["mem.used"] = application.metrics["mem"] - $scope.application.metrics["mem.free"];
  				
  				$scope.gcInfos = {};
  				for (var metric in application.metrics) {
  					var matchTime = /gc\.(.+)\.time/.exec(metric);
  					if ( matchTime !== null) {
  						var gcName = matchTime[1];
  						if ($scope.gcInfos[gcName] == null) $scope.gcInfos[gcName] = {};
  						$scope.gcInfos[gcName].time = application.metrics[metric];
  					}
  					
  					var matchCount = /gc\.(.+)\.count/.exec(metric);
  					if ( matchCount !== null) {
  						var gcName = matchCount[1];
  						if ($scope.gcInfos[gcName] == null) $scope.gcInfos[gcName] = {};
  						$scope.gcInfos[gcName].count = application.metrics[metric];
  					}
  					
  				}
  				
  			});
  		});
  		
  		var start = Date.now();
  		var tick = $interval(function() {
  			$scope.ticks = Date.now() - start;
  		},1000);
  	}])
  	.controller('detailsMetricsCtrl',  ['$scope', '$stateParams', 'Application', 'ApplicationDetails', 'Abbreviator', 
  	                                    function ($scope, $stateParams, Application, ApplicationDetails, Abbreviator) {
  		$scope.memoryData = [];
  		$scope.heapMemoryData = [];
  		$scope.counterData = [];
  		$scope.gaugeData = [];
  		
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getMetrics(application, function(application) {
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
  		
  	}])
  	.controller('detailsEnvCtrl',  ['$scope', '$stateParams', 'Application', 'ApplicationDetails', 
  	                                function ($scope, $stateParams, Application, ApplicationDetails) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getEnv(application);
  		});
  	}])
  	.controller('detailsPropsCtrl', ['$scope', '$stateParams', 'Application', 'ApplicationDetails', 
  			function ($scope, $stateParams, Application, ApplicationDetails) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getProps(application);
  		});
  	}])
  	.controller('detailsClasspathCtrl',  ['$scope', '$stateParams', 'Application', 'ApplicationDetails', 'Abbreviator', 
  	                                      function ($scope, $stateParams, Application, ApplicationDetails, Abbreviator) {
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationDetails.getClasspath(application);
  		});
  	}])
  	.controller('loggingCtrl',  ['$scope', '$stateParams', 'Application', 'ApplicationLogging', 
  	                             function ($scope, $stateParams, Application, ApplicationLogging) {
  		$scope.loggers = [];
  		$scope.filteredLoggers = [];
  		$scope.limit = 10;
  		
  		function findLogger(loggers, name) {
  			for(var i in loggers) {
  				if (loggers[i].name === name){
  					return loggers[i];
  				}
  			}
  		}
  		
		$scope.setLogLevel = function(name, level) {
			ApplicationLogging.setLoglevel($scope.application, name, level).then(function(response){
				$scope.reload(name);
			}).catch(function(response){
				$scope.error = response.error;
				console.log(response.stacktrace)
				$scope.reload(name);
			})
  		} 
		
  		$scope.reload = function(prefix) {
  			for (var i in $scope.loggers) {
  				if (prefix == null || prefix === 'ROOT' || $scope.loggers[i].name.indexOf(prefix) == 0 ){
  					$scope.loggers[i].level = null;
  				}
  			}
  			$scope.refreshLevels();
  		} 
  		
  		$scope.refreshLevels = function() {
  			var toLoad = [];
  			var slice = $scope.filteredLoggers.slice(0, $scope.limit);
  			for (var i in slice ) {
  				if (slice[i].level === null) {
  					toLoad.push(slice[i]);
  				}
  			}

  			if (toLoad.length == 0) return;
  			
  			ApplicationLogging.getLoglevel($scope.application, toLoad).then(
  					function(responses) {
  						for (var i in responses) {
  							var name = responses[i].request.arguments[0];
  							var level = responses[i].value;
  							findLogger($scope.loggers, name).level = level;
  						}
  					}).catch(function(responses){
  						for (var i in responses) {
  							if (responses[i].error != null) {
  								$scope.error = responses[i].error;
  								console.log(responses[i].stacktrace);
  								break;
  							}
  						}
  					});
  		} 
  		
  		$scope.application = Application.query({id: $stateParams.id}, function(application) {
  			ApplicationLogging.getAllLoggers(application).then( function (response) {
  				$scope.loggers  = [];
  				for (var i in response.value) {
  					$scope.loggers .push({name: response.value[i], level: null});
  				}
  				
  		  		$scope.$watchCollection('filteredLoggers', function() {
  		  			$scope.refreshLevels();
  		  		});

  		  		$scope.$watch('limit', function() {
		  			$scope.refreshLevels();
		  		});
  			}).catch(function(response) {
  				$scope.error = response.error;
  				console.log(response.stacktrace);
  			})
  		});
  	}])
  	.controller('jmxCtrl',  ['$scope', '$stateParams', '$modal', 'Application', 'ApplicationJMX', 
  	                         function ($scope, $stateParams, $modal, Application, ApplicationJMX) {
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
  		
  		$scope.readAllAttr = function(bean) {
  			bean.error = null;
  			ApplicationJMX.readAllAttr($scope.application, bean).then(
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
  	}]);
