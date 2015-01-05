'use strict';

var angular = require('angular');
var springBootAdmin = angular.module('springBootAdmin');

springBootAdmin.factory('Applications', require('./applications'));
springBootAdmin.factory('Application', require('./application'));

springBootAdmin.service('ApplicationOverview', require('./applicationOverview'));
springBootAdmin.service('ApplicationDetails', require('./applicationDetails'));
springBootAdmin.service('ApplicationLogging', require('./applicationLogging'));
springBootAdmin.service('ApplicationJMX', require('./applicationJmx'));
springBootAdmin.service('Abbreviator', require('./abbreviator'));
springBootAdmin.service('jolokia', require('./jolokia'));
springBootAdmin.service('MetricsHelper', require('./metricsHelper'));
springBootAdmin.service('ApplicationThreads', require('./applicationThreads'));
