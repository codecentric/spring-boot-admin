'use strict';

module.exports = function ($scope, application) {
    $scope.javaMelodyUrl = '/javamelody?application=' + application.name + '-' + application.id;
};
