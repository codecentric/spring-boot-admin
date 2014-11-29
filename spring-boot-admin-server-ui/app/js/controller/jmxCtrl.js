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

module.exports = function ($scope, $modal, $log, application, ApplicationJMX) {
    $scope.error = null;
    $scope.domains = [];

    ApplicationJMX.list(application)
        .then(function (domains) {
            $scope.domains = domains;
        })
        .catch(function (response) {
            $scope.error = response.error;
            $log.error(response.stacktrace);
        });

    $scope.readAllAttr = function (bean) {
        bean.error = null;
        ApplicationJMX.readAllAttr(application, bean)
            .then(
                function (response) {
                    for (var name in response.value) {
                        bean.attributes[name].error = null;
                        bean.attributes[name].value = response.value[name];
                        bean.attributes[name].jsonValue = JSON.stringify(response.value[
                            name], null, '   ');
                    }
                })
            .catch(function (response) {
                bean.error = response.error;
                $log.error(response.stacktrace);
            });
    };

    $scope.writeAttr = function (bean, name, attr) {
        attr.error = null;
        ApplicationJMX.writeAttr(application, bean, name, attr.value)
            .catch(
                function (response) {
                    attr.error = response.error;
                    $log.error(response.stacktrace);
                });
    };

    $scope.invoke = function () {
        $scope.invocation.state = 'executing';

        ApplicationJMX.invoke(application, $scope.invocation.bean, $scope.invocation.opname,
                $scope.invocation.args)
            .then(
                function (response) {
                    $scope.invocation.state = 'success';
                    $scope.invocation.result = response.value;
                })
            .catch(function (response) {
                $scope.invocation.state = 'error';
                $scope.invocation.error = response.error;
                $scope.invocation.stacktrace = response.stacktrace;
            });

        $modal.open({
                templateUrl: 'invocationResultDialog.html',
                scope: $scope
            })
            .result.then(function () {
                $scope.invocation = null;
            });
    };

    $scope.prepareInvoke = function (bean, name, op) {
        $scope.invocation = {
            bean: bean,
            opname: name,
            opdesc: op,
            args: [],
            state: 'prepare'
        };

        if (op instanceof Array) {
            $modal.open({
                    templateUrl: 'invocationVariantDialog.html',
                    scope: $scope
                })
                .result.then(function (chosenOp) {
                    $scope.prepareInvoke(bean, name, chosenOp);
                })
                .catch(function () {
                    $scope.invocation = null;
                });
        } else {
            if (op.args.length === 0) {
                $scope.invoke();
            } else {
                var signature = '(';
                for (var i in op.args) {
                    if (i > 0) {
                        signature += ',';
                    }
                    signature += op.args[i].type;
                    $scope.invocation.args[i] = null;
                }
                signature += ')';
                $scope.invocation.opname = name + signature;

                $modal.open({
                        templateUrl: 'invocationPrepareDialog.html',
                        scope: $scope
                    })
                    .result.then(function () {
                        $scope.invoke();
                    })
                    .catch(function () {
                        $scope.invocation = null;
                    });
            }
        }
    };
};
