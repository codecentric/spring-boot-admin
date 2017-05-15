'use strict';

module.exports = function ($scope, application) {
    'ngInject';
    $scope.error = null;
    $scope.javaMelodyUrl = '/javamelody?application=' + application.name + '-' + application.id;
};
