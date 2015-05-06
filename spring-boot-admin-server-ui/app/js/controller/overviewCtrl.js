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

module.exports = function ($scope, $location, $interval, $q, $state, Application, Notification) {
    var createNote = function(app) {
        var title = app.name + (app.status === 'UP' ? ' is back ' : ' went ') + app.status;
        var options = { tag: app.id,
                        body: 'Instance ' + app.id,
                        icon: (app.status === 'UP' ? 'img/ok.png' : 'img/error.png'),
                        timeout: 150000,
                        url: $state.href('apps.details', {id: app.id}, {absolute: true}) };
        Notification.notify(title, options);
    };

    var getInfo = function (app) {
        return app.getInfo()
            .success(function (response) {
                app.version = response.version;
                delete response.version;
                app.info = response;
            })
            .error(function () {
                app.version = '---';
            });
    };
    var getHealth = function (app) {
        var oldstatus = app.status;

        return app.getHealth()
            .success(function (response) {
                app.status = response.status || 'UP';
                if (oldstatus !== undefined && oldstatus !== app.status) {
                    createNote(app);
                }
            })
            .error(function (response, httpStatus) {
                if (httpStatus === 503) {
                    app.status = response.status;
                } else if (httpStatus === 404 || httpStatus === 0 || (httpStatus === 500 && response.exception === 'java.net.ConnectException')) {
                    app.status = 'OFFLINE';
                } else {
                    app.status = 'UNKNOWN';
                }
                if (oldstatus !== undefined && oldstatus !== app.status) {
                    createNote(app);
                }
            });
    };

    $scope.loadData = function () {
        Application.query(function (applications) {
            function refresh(app) {
                //find application in known applications and copy state --> less flickering
                for (var j = 0; $scope.applications != null && j < $scope.applications.length; j++) {
                    if (app.id === $scope.applications[j].id) {
                        app.info = $scope.applications[j].info;
                        app.version = $scope.applications[j].version;
                        app.status = $scope.applications[j].status;
                        break;
                    }
                }
                app.refreshing = true;
                $q.all(getInfo(app), getHealth(app))
                    .finally(function () {
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
    }, 30000);
};
