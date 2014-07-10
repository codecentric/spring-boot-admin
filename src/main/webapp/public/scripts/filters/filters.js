'use strict';

angular.module('springBootAdmin')
	.filter('ceil', function() { 
		return function(input) { 
			return Math.ceil(input); 
		} 
	});