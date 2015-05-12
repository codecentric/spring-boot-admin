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

module.exports = function ($scope, application) {
    $scope.application = application;
    $scope.overrides = { values: [{key: '', value: '' }], error: null};

    var toArray = function(map) {
        var array = [];
        for (var key in map) {
            var value = map[key];
            if (value instanceof Object) {
                value = toArray(value);
            }
            var entry = {key: key, value: value};
            array.push(entry);
        }
        return array.length > 0 ? array : undefined;
    };

    var collectKeys = function(envArray) {
        var allKeys = {};
        for (var i = 0; i < envArray.length; i++) {
            for (var j = 0; envArray[i].value && j < envArray[i].value.length; j++) {
                allKeys[envArray[i].value[j].key] = true;
            }
        }
        return Object.getOwnPropertyNames(allKeys);
    };

    $scope.reload = function() {
        $scope.env = {};
        $scope.envArray = [];
        $scope.allKeys = [];

        application.getEnv()
          .success(function (env) {
            $scope.env = env;
            $scope.envArray = toArray(env);
            $scope.allKeys = collectKeys($scope.envArray);
        })
        .error(function (error) {
            $scope.error = error;
        });
    };

    $scope.reload();

    var getValue = function(item) {
        if (item.key && $scope.allKeys.indexOf(item.key) >= 0) {
            application.getEnv(item.key).success(function(value) {
                item.value = value;
            });
        }
    };

    $scope.onChangeOverrideItem = function(item) {
        getValue(item);

        if ($scope.overrides.values[$scope.overrides.values.length - 1].key) {
            $scope.overrides.values.push({key: '', value: '' });
        }
    };

    $scope.override = function() {
        var map = {};
        for (var i = 0; i < $scope.overrides.values.length; i++) {
            if ($scope.overrides.values[i].key) {
                map[$scope.overrides.values[i].key] = $scope.overrides.values[i].value;
            }
        }

        $scope.overrides.error = null;
        application.setEnv(map).success(function () {
            $scope.overrides = { values: [{key: '', value: '' }], error: null, changes: null};
            $scope.reload();
         })
        .error(function (error) {
            $scope.overrides.error = error;
            $scope.overrides.changes = null;
            $scope.reload();
        });
    };

    $scope.reset = function() {
        $scope.overrides.error = null;
        $scope.overrides.changes = null;
        application.resetEnv().success(function () {
            $scope.reload();
         })
        .error(function (error) {
            $scope.overrides.error = error;
            $scope.reload();
        });

    };

    $scope.refresh = function() {
        $scope.overrides.error = null;
        $scope.overrides.changes = null;
        application.refresh().success(function (changes) {
            $scope.overrides.changes = changes;
            $scope.reload();
         })
        .error(function (error) {
            $scope.overrides.error = error;
            $scope.reload();
        });

    };
};
