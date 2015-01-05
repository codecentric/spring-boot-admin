'use strict';

var angular = require('angular');
var springBootAdmin = angular.module('springBootAdmin');

springBootAdmin.directive('richMetricBar', require('./richMetricBar'));
springBootAdmin.directive('simpleMetricBar', require('./simpleMetricBar'));
