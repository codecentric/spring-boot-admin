/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

module.exports = function ($scope, $location, $interval, $state, $filter, Application, Notification) {
    var createNote = function(app) {
        var title = app.name + (app.statusInfo.status === 'UP' ? ' is back ' : ' went ') + app.statusInfo.status;
        var options = { tag: app.id,
                        body: 'Instance ' + app.id,
                        icon: (app.statusInfo.status === 'UP' ? 'img/ok.png' : 'img/error.png'),
                        timeout: 15000,
                        url: $state.href('apps.details', {id: app.id}) };
        Notification.notify(title, options);
    };

    $scope.loadData = function () {
        Application.query(function (applications) {
            function refresh(app) {
                app.refreshing = true;
                app.info = {};

                //find application in known applications and copy state --> less flickering
                for (var j = 0; $scope.applications != null && j < $scope.applications.length; j++) {
                    if (app.id === $scope.applications[j].id) {
                        app.infoShort = $scope.applications[j].infoShort;
                        app.infoDetails = $scope.applications[j].infoDetails;
                        app.version = $scope.applications[j].version;
                        //issue notifiaction on state change
                        if (app.statusInfo.status !== $scope.applications[j].statusInfo.status) {
                            createNote(app);
                        }
                        break;
                    }
                }
                app.getInfo().then(function(info) {
                    app.version = info.version;
                    app.infoDetails = null;
                    app.infoShort = '';
                    delete info.version;
                    var infoYml = $filter('yaml')(info);
                    if (infoYml !== '{}\n') {
                        app.infoShort = $filter('limitLines')(infoYml, 3);
                        if (app.infoShort !== infoYml) {
                           app.infoDetails = $filter('limitLines')(infoYml, 32000, 3);
                        }
                    }
                }).finally(function(){
                    app.refreshing = false;
                });
            }

            for (var i = 0; i < applications.length; i++) {
                refresh(applications[i]);
            }
            $scope.applications = applications;
        });
    };

    $scope.remove = function (application) {
        application.$remove(function () {
            var index = $scope.applications.indexOf(application);
            if (index > -1) {
                $scope.applications.splice(index, 1);
            }
        });
    };

    $scope.order = {
        column: 'name',
        descending: false
    };

    $scope.orderBy = function (column) {
        if (column === $scope.order.column) {
            $scope.order.descending = !$scope.order.descending;
        } else {
            $scope.order.column = column;
            $scope.order.descending = false;
        }
    };

    $scope.orderByCssClass = function (column) {
        if (column === $scope.order.column) {
            return 'sorted-' + ($scope.order.descending ? 'descending' : 'ascending');
        } else {
            return '';
        }
    };

    //initial load
    $scope.loadData();

    // reload site every 30 seconds
    $interval(function () {
        $scope.loadData();
    }, 10000);
};
