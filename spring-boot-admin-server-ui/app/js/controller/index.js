'use strict';

var angular = require('angular');
var springBootAdmin = angular.module('springBootAdmin');

springBootAdmin.controller('overviewCtrl', require('./overviewCtrl'));
springBootAdmin.controller('appsCtrl', require('./appsCtrl'));
springBootAdmin.controller('detailsCtrl', require('./detailsCtrl'));
springBootAdmin.controller('detailsMetricsCtrl', require('./detailsMetricsCtrl'));
springBootAdmin.controller('detailsEnvCtrl', require('./detailsEnvCtrl'));
springBootAdmin.controller('detailsPropsCtrl', require('./detailsPropsCtrl'));
springBootAdmin.controller('detailsClasspathCtrl', require('./detailsClasspathCtrl'));
springBootAdmin.controller('loggingCtrl', require('./loggingCtrl'));
springBootAdmin.controller('jmxCtrl', require('./jmxCtrl'));
springBootAdmin.controller('threadsCtrl', require('./threadsCtrl'));
