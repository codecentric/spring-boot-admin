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

module.exports = function ($scope, $location, $interval, $q, Applications, ApplicationOverview,
    Application) {
    $scope.loadData = function () {
        Applications.query(function (applications) {
            function refresh(app) {
                //find application in known applications and copy state --> less flickering
                for (var j = 0; $scope.applications != null && j < $scope.applications
                    .length; j++) {
                    if (app.id === $scope.applications[j].id) {
                        app.info = $scope.applications[j].info;
                        app.version = $scope.applications[j].version;
                        app.status = $scope.applications[j].status;
                        app.providesLogfile = $scope.applications[j].providesLogfile;
                        break;
                    }
                }
                app.refreshing = true;
                $q.all(ApplicationOverview.getInfo(app),
                        ApplicationOverview.getHealth(app),
                        ApplicationOverview.getLogfile(app))
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
    $scope.loadData();

    $scope.remove = function (application) {
        Application.remove({
            id: application.id
        }, function () {
            var index = $scope.applications.indexOf(application);
            if (index > -1) {
                $scope.applications.splice(index, 1);
            }
        });
    };

    // reload site every 30 seconds
    $interval(function () {
        $scope.loadData();
    }, 30000);
};
