'use strict';

var angular = require('angular');
var springBootAdmin = angular.module('springBootAdmin');

springBootAdmin.filter('timeInterval', require('./timeInterval'));
springBootAdmin.filter('classNameLoggerOnly', require('./classNameLoggerOnly'));
springBootAdmin.filter('capitalize', require('./capitalize'));
springBootAdmin.filter('humanBytes', require('./humanBytes'));
