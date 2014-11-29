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

module.exports = function ($scope, application, ApplicationThreads) {
    $scope.dumpThreads = function () {
        ApplicationThreads.getDump(application)
            .success(function (dump) {
                $scope.dump = dump;

                var threadStats = {
                    NEW: 0,
                    RUNNABLE: 0,
                    BLOCKED: 0,
                    WAITING: 0,
                    TIMED_WAITING: 0,
                    TERMINATED: 0
                };
                for (var i = 0; i < dump.length; i++) {
                    threadStats[dump[i].threadState] ++;
                }
                threadStats.total = dump.length;
                $scope.threadStats = threadStats;
            })
            .error(function (error) {
                $scope.error = error;
            });
    };
};
