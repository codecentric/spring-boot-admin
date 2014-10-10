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

angular.module('springBootAdmin.services', ['ngResource'])
  	.factory('Applications', ['$resource', function($resource) {
  		return $resource(
  			'/api/applications', {}, {
  				query: { method:'GET', isArray:true }
  			});
  		}
  	])
  	.factory('Application', ['$resource', function($resource) {
  		return $resource(
  			'/api/application/:id', {}, {
  				query: { method:'GET'}
  			});
  		}
  	])
  	.service('ApplicationOverview', ['$http', function($http) {
  		this.getVersion = function(app) {
  			return $http.get(app.url + '/info').success(function(response) {
  				app.version = response.version;
  			}).error(function() {
  				app.version = '---';
  			});
  		}
  		this.getHealth = function(app) {
  			return $http.get(app.url + '/health').success(function(response) {
  				if (typeof(response) === 'string' && response.indexOf('ok') != -1 
  						|| typeof(response.status) === 'string' && 
							(response.status.indexOf('ok') != -1 || response.status.indexOf('UP') != -1)) { 
  					app.up = true;
  				} else if (typeof(response.status) === 'string' && response.status.indexOf('DOWN') != -1) { 
  					app.down = true;
  				} else if (typeof(response.status) === 'string' && response.status.indexOf('OUT-OF-SERVICE') != -1) { 
  					app.outofservice = true;
  				} else {
  					app.unknown = true;
  				}
  			}).error(function() {
  				app.offline = true;
  			});
  		}
  		this.getLogfile = function(app) {
  			return $http.get(app.url + '/logfile').success(function(response) {
  				app.providesLogfile = true;
  				app.urlLogfile = app.url + '/logfile';
  			}).error(function() {
  				app.providesLogfile = false;
  				app.urlLogfile = null;
  			});
  		}
  		this.refresh = function(app) {
  			return $http.post(app.url + '/refresh');
  		}
  	}])
  	.service('ApplicationDetails', ['$http', function($http) {
  		this.getInfo = function(app) {
  			return $http.get(app.url + '/info').success(function(response) {
  				app.info = response;
  			});
  		}
  		this.getMetrics = function(app, success) {
  			return $http.get(app.url + '/metrics').success(function(response) {
  				app.metrics = response;
  				success(app);
  			});
  		}
  		this.getEnv = function(app) {
  			return $http.get(app.url + '/env').success(function(response) {
  				app.env = response;
  			});
  		}
  		this.getProps = function(app) {
  			return $http.get(app.url + '/env').success(function(response) {
  				app.props = [];
  				for (var attr in response) {
  					if (attr.indexOf('[') != -1 && attr.indexOf('.properties]') != -1) {
  						var prop = new Object();
  						prop.key = attr;
  						prop.value = response[attr];
  						app.props.push(prop);
  					}
  				}
  			});
  		}
  		this.getClasspath = function(app) {
  			return $http.get(app.url + '/env').success(function(response) {
  				app.classpath = response['systemProperties']['java.class.path'].split(":");
  			});
  		}
  	}])
  	.service('ApplicationLogging', ['$http', function($http) {
  		this.getLoglevel = function(app) {
  			return $http.get(app.url + 
  					'/jolokia/exec/ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator/getLoggerLevel/' + 
  					app.logger.name)
  				.success(function(response) {
  					if (response['value'].length > 0) {
  						app.logger.loglevel = response['value'];
  					} else {
  						app.logger.loglevel = '<unknown>';
  					}
  				});
  		}
  		this.setLoglevel = function(app) {
  			return $http.get(app.url + 
  					'/jolokia/exec/ch.qos.logback.classic:Name=default,Type=ch.qos.logback.classic.jmx.JMXConfigurator/setLoggerLevel/' + 
  					app.logger.name + '/' + app.logger.loglevel)
  				.success(function(response) {
  					if (response['status'] == 200) {
  						app.logger.success = true;
  					} else {
  						app.logger.success = false;
  					}
  				});
  		}
  	}])
  	.service('Abbreviator', [function() {
  		  function _computeDotIndexes(fqName, delimiter, preserveLast) {
		    var dotArray = [];
		    
		    //iterate over String and find dots
		    var lastIndex = -1;
		    do {
		      lastIndex = fqName.indexOf(delimiter, lastIndex + 1);
		      if (lastIndex !== -1) {
		        dotArray.push(lastIndex);
		      }
		    } while (lastIndex !== -1)

		    // remove dots to preserve more than the last element
		    for (var i = 0; i < preserveLast -1; i++ ) {
		    	dotArray.pop();
		    }
		    	
		    return dotArray;
		  }

		  function _computeLengthArray(fqName, targetLength, dotArray, shortenThreshold) {
			var lengthArray = [];
		    var toTrim = fqName.length - targetLength;

		    for (var i = 0; i < dotArray.length; i++) {
		      var previousDotPosition = -1;
		      if (i > 0) {
		        previousDotPosition = dotArray[i - 1];
		      }

		      var len = dotArray[i] - previousDotPosition - 1;
		      var newLen = (toTrim > 0 && len > shortenThreshold ? 1 : len);
		      
		      toTrim -= (len - newLen);
		      lengthArray[i] = newLen + 1;
		    }

		    var lastDotIndex = dotArray.length - 1;
		    lengthArray[dotArray.length] = fqName.length - dotArray[lastDotIndex];
		    
		    return lengthArray;
		  }
  		
  		this.abbreviate = function(fqName, delimiter, targetLength, preserveLast, shortenThreshold) {
  		    if (fqName.length < targetLength) {
  		      return fqName;
  		    }

  		    var dotIndexesArray = _computeDotIndexes(fqName, delimiter, preserveLast);

  		    if (dotIndexesArray.length === 0) {
  		      return fqName;
  		    }

  		    var lengthArray = _computeLengthArray(fqName, targetLength, dotIndexesArray, shortenThreshold);

  		    var result = "";
  		    for (var i = 0; i <= dotIndexesArray.length; i++) {
  		    	if (i === 0 ) {
  		    		result += fqName.substr(0, lengthArray[i] -1);
  		    	} else {
  		    		result += fqName.substr(dotIndexesArray[i - 1], lengthArray[i]);
  		    	}
  		    }
  		    
  		    return result;
  		}
  	}]);
