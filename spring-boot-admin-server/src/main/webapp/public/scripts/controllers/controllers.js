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
       .controller('overviewCtrl', ['$scope', '$location', '$interval', 'Applications', 'ApplicationOverview', 'Application',
                                    function ($scope, $location, $interval, Applications, ApplicationOverview, Application) {
  		$scope.loadData = function() {
  			Applications.query(function(applications) {
  				for (var i = 0; i< applications.length; i++) {
	  				var app = applications[i];
	  				ApplicationOverview.getInfo(app);
	  				ApplicationOverview.getHealth(app);
	  				ApplicationOverview.getLogfile(app);
	  			}	
	  			$scope.applications = applications;
	  		});
  		}
  		$scope.loadData();

  		$scope.refresh = function(application) {
  			ApplicationOverview.refresh(application);
  		};
  		
  		$scope.remove = function(application) {
  			Application.remove({ id: application.id }, function () {
  				var index = $scope.applications.indexOf(application); 
  				if (index > -1) {
  					$scope.applications.splice(index, 1);
  				}
  			});
  		}

  		
  		// reload site every 30 seconds
  		var task = $interval(function() {
  			$scope.loadData();
  		}, 30000);
  	}])
  	.controller('appsCtrl',  ['$scope', 'application', function ($scope, application) {
  		$scope.application = application;
  	}])  	
  	.controller('detailsCtrl', ['$scope', '$interval', 'application', 'ApplicationDetails', 'MetricsHelper',
  	                            function ($scope, $interval, application, ApplicationDetails, MetricsHelper) {
  		$scope.application = application;
  		ApplicationDetails.getInfo(application).success(function(info) {
			$scope.info = info;
		}).error( function(error) {
			$scope.error = error;
		});
  			
		ApplicationDetails.getHealth(application).success(function(health) {
			$scope.health = health;
		}).error( function(health) {
			$scope.health = health;
		});
  			
		ApplicationDetails.getMetrics(application).success(function(metrics) {
			$scope.metrics = metrics;
			$scope.metrics["mem.used"] = $scope.metrics["mem"] - $scope.metrics["mem.free"];
  				
			$scope.gcInfos = {};
			$scope.datasources = {}
  				
			function createOrGet(map, key, factory) {
				return map[key] || (map[key] = factory());
			}
  				
			MetricsHelper.find(metrics,
					[ /gc\.(.+)\.time/, /gc\.(.+)\.count/, /datasource\.(.+)\.active/,  /datasource\.(.+)\.usage/ ],
					[ function(metric, match, value) {
						createOrGet($scope.gcInfos, match[1], function() {return {time: 0, count: 0};}).time = value;
					 },
					function(metric, match, value) {
						createOrGet($scope.gcInfos, match[1], function() {return {time: 0, count: 0};}).count = value;
					 },
					function(metric, match, value) {
						$scope.hasDatasources = true;
						createOrGet($scope.datasources, match[1], function() {return {min: 0, max:0, active: 0, usage: 0};}).active = value;
					 },
					function(metric, match, value) {
						$scope.hasDatasources = true;
						createOrGet($scope.datasources, match[1], function() {return {min: 0, max:0, active: 0, usage: 0};}).usage = value;
			 }]);
		}).error( function(error) {
			$scope.error = error;
		});
  		
  		var start = Date.now();
  		var tick = $interval(function() {
  			$scope.ticks = Date.now() - start;
  		},1000);
  	}])
  	.controller('detailsMetricsCtrl',  ['$scope', 'application', 'ApplicationDetails', 'Abbreviator', 'MetricsHelper',
  	                                    function ($scope, application, ApplicationDetails, Abbreviator, MetricsHelper) {
  		$scope.memoryData = [];
  		$scope.heapMemoryData = [];
  		$scope.counterData = [];
  		$scope.gaugeData = [];
  		
		ApplicationDetails.getMetrics(application).success(function(metrics) {
			//*** Extract data for Counter-Chart and Gauge-Chart
			$scope.counterData = [ { key : "value", values: [] } ];
			$scope.gaugeData = [ { key : "value", values: []   }, 
			                     { key : "average", values: [] },
								 { key : "min", values: []     },
								 { key : "max", values: []     },
			                     { key : "count", values: []   } ];
		
			MetricsHelper.find(metrics, 
					[ /counter\.(.+)/, /gauge\.(.+)\.val/, /gauge\.(.+)\.avg/,  /gauge\.(.+)\.min/,  /gauge\.(.+)\.max/,  /gauge\.(.+)\.count/,   /gauge\.(.+)\.alpha/,  /gauge\.(.+)/], 
					[ function (metric, match, value) { $scope.counterData[0].values.push([ match[1], value]); },
					function (metric, match, value) { $scope.gaugeData[0].values.push([ match[1], value]); },
					function (metric, match, value) { $scope.gaugeData[1].values.push([ match[1], value]); },
					function (metric, match, value) { $scope.gaugeData[2].values.push([ match[1], value]); },
					function (metric, match, value) { $scope.gaugeData[3].values.push([ match[1], value]); },
					function (metric, match, value) { $scope.gaugeData[4].values.push([ match[1], value]); },
					function (metric, match, value) { /*NOP*/ },
					function (metric, match, value) { $scope.gaugeData[0].values.push([ match[1], value]); }]);
		
			//in case no richGauges are present remove empty groups
			var i = $scope.gaugeData.length;
			while (--i) {
				if ($scope.gaugeData[i].values.length === 0) {
					$scope.gaugeData.splice(i, 1);
				}
			}
		}).error( function(error) {
			$scope.error = error;
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
  	.controller('detailsEnvCtrl',  ['$scope', 'application', 'ApplicationDetails', 
  	                                function ($scope, application, ApplicationDetails) {
  		$scope.application = application;
  		ApplicationDetails.getEnv(application).success(function(env) {
			$scope.env = env;
		}).error( function(error) {
			$scope.error = error;
  		});
  	}])
  	.controller('detailsPropsCtrl', ['$scope', 'application', 'ApplicationDetails', 
  			function ($scope, application, ApplicationDetails) {
  		$scope.application = application;
  		ApplicationDetails.getEnv(application).success(function(env) {
			$scope.props = [];
			for (var attr in env) {
				if (attr.indexOf('[') != -1 && attr.indexOf('.properties]') != -1) {
					$scope.props.push({ key : attr, value: env[attr] });
				}
			}
		}).error( function(error) {
			$scope.error = error;
		});
  	}])
  	.controller('detailsClasspathCtrl',  ['$scope', 'application', 'ApplicationDetails', 'Abbreviator', 
  	                                      function ($scope, application, ApplicationDetails, Abbreviator) {
  		$scope.application = application;
  		ApplicationDetails.getEnv(application).success(function(env) {
			var separator =  env['systemProperties']['path.separator'];
			$scope.classpath = env['systemProperties']['java.class.path'].split(separator);
		}).error( function(error) {
			$scope.error = error;
		});
  	}])
  	.controller('loggingCtrl',  ['$scope', 'application', 'ApplicationLogging', 
  	                             function ($scope, application, ApplicationLogging) {
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
			ApplicationLogging.setLoglevel(application, name, level).then(function(response){
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
  			
  			ApplicationLogging.getLoglevel(application, toLoad).then(
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
  	}])
  	.controller('jmxCtrl',  ['$scope', '$modal', 'application', 'ApplicationJMX', 
  	                         function ($scope, $modal, application, ApplicationJMX) {
		$scope.error = null;
		$scope.domains = [];
		
		ApplicationJMX.list(application).then(function(domains){
			$scope.domains = domains;
		}).catch(function(response) {
			$scope.error = response.error;
			console.log(response.stacktrace);  				
		})
  		
  		$scope.readAllAttr = function(bean) {
  			bean.error = null;
  			ApplicationJMX.readAllAttr(application, bean).then(
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
  			ApplicationJMX.writeAttr(application, bean, name, attr.value).catch(
  					function(response) {
  						attr.error = response.error;
  						console.log(response.stacktrace);
  					});
  		}
  		
  		$scope.invoke = function() {
  			$scope.invocation.state = 'executing';
  			
  			ApplicationJMX.invoke(application, $scope.invocation.bean, $scope.invocation.opname, $scope.invocation.args).then(
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
  	}])
  	.controller('threadsCtrl',  ['$scope', 'application', 'ApplicationThreads',
  	                                function ($scope, application, ApplicationThreads) {
  		$scope.dumpThreads = function() {
  			ApplicationThreads.getDump(application).success(function(dump) {
	  			$scope.dump = dump;
	  			
	  			var threadStats = { NEW: 0, RUNNABLE: 0, BLOCKED: 0, WAITING: 0, TIMED_WAITING: 0, TERMINATED: 0};
	  			for (var i = 0; i < dump.length; i++) {
	  				threadStats[dump[i].threadState]++;
	  			}
	  			threadStats.total = dump.length;
	  			$scope.threadStats = threadStats;
	  			
	  		}).error( function(error) {
	  			$scope.error = error;
	  		});
  		}
  	}]);
