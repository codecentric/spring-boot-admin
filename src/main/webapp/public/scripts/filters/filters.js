'use strict';

angular.module('springBootAdmin')
	.filter('ceil', function() { 
		return function(input) { 
			return Math.ceil(input); 
		} 
	})
	.filter('floor', function() { 
		return function(input) { 
			return Math.floor(input); 
		} 
	})
	.filter('padZero', function() { 
		return function(input) { 
			var s = input + "";
		    while (s.length < 2) s = "0" + s;
		    return s;
		} 
	});