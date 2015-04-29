'use strict';

var angular = require('angular');
var springBootAdmin = angular.module('springBootAdmin');

springBootAdmin.factory('Application', require('./application'));
springBootAdmin.service('ApplicationLogging', require('./applicationLogging'));
springBootAdmin.service('ApplicationJMX', require('./applicationJmx'));
springBootAdmin.service('Abbreviator', require('./abbreviator'));
springBootAdmin.service('jolokia', require('./jolokia'));
springBootAdmin.service('MetricsHelper', require('./metricsHelper'));
springBootAdmin.service('Notification', require('./notification'));
