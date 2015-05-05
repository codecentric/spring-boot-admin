'use strict';

var angular = require('angular');
var springBootAdmin = angular.module('springBootAdmin');

springBootAdmin.controller('overviewCtrl', require('./overviewCtrl'));
springBootAdmin.controller('appsCtrl', require('./appsCtrl'));
springBootAdmin.controller('detailsCtrl', require('./apps/detailsCtrl'));
springBootAdmin.controller('detailsMetricsCtrl', require('./apps/details/metricsCtrl'));
springBootAdmin.controller('detailsClasspathCtrl', require('./apps/details/classpathCtrl'));
springBootAdmin.controller('environmentCtrl', require('./apps/environmentCtrl'));
springBootAdmin.controller('activitiCtrl', require('./apps/activitiCtrl'));
springBootAdmin.controller('loggingCtrl', require('./apps/loggingCtrl'));
springBootAdmin.controller('jmxCtrl', require('./apps/jmxCtrl'));
springBootAdmin.controller('threadsCtrl', require('./apps/threadsCtrl'));
springBootAdmin.controller('traceCtrl', require('./apps/traceCtrl'));
