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

module.exports = function ($scope, application, $interval) {
    $scope.traces = [];
    $scope.refresher = null;
    $scope.refreshInterval = 5;

    $scope.refresh = function() {
        application.getTraces().then(function(traces) {
            var oldestTrace = traces[traces.length - 1];
            var olderTraces = $scope.traces.filter(function(trace) {
                return trace.timestamp < oldestTrace.timestamp;
            });
            $scope.traces = traces.concat(olderTraces);
        }).catch(function(error) {
            $scope.error = error;
        });
    };

    $scope.toggleAutoRefresh = function() {
        if ($scope.refresher === null) {
            $scope.refresher = $interval(function () {
                $scope.refresh();
            }, $scope.refreshInterval * 1000);
        } else {
            $interval.cancel($scope.refresher);
            $scope.refresher = null;
        }
    };

    /**
     * Determines the css class for a trace-item.
     *
     * @param trace current item
     */
    $scope.getStatusClass = function (trace) {
        // only if the response part is available
        if (trace.info.headers.response) {
            var status = parseInt(trace.info.headers.response.status);

            if (Math.floor(status / 500) === 1) {
                return 'error';
            } else if (Math.floor(status / 400) === 1) {
                return 'warn';
            }
            return 'ok';
        }

        return 'unknown';
    };

    $scope.refresh();
};
