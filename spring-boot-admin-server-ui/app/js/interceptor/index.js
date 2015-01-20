'use strict';

var angular = require('angular');
var springBootAdmin = angular.module('springBootAdmin');

springBootAdmin.factory('AuthInterceptor', require('./authInterceptor'));
