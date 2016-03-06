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

module.exports = {
        bindings : {
            env : '<environment',
            application : '<application',
            envChangedCallback : '&onEnvironmentChanged'
        },
        controller : function() {
            var ctrl = this;
            ctrl.error = null;

            ctrl.$onChanges = function() {
                if (ctrl.env) {
                    ctrl.allProperties = ctrl.env.reduce(function(left, right) {
                        return left.concat(right.value);
                    }, []);
                    
                    var managerSource = ctrl.env.find(function(source) {
                        return source.name === 'manager';
                    });
                    ctrl.newProperties = (managerSource ? managerSource.value.slice(0) : []);
                    
                    ctrl.newProperties.push({
                        name : null,
                        value : null
                    });
                    
                    ctrl.allPropertyNames = ctrl.allProperties.map(function(property) {
                        return property.name;
                    }).sort().filter(function(name, index, names) {
                        return !name || name !== names[index - 1];
                    });
                }
            };

            ctrl.onChangePropertyName = function(edited) {
                var property = ctrl.allProperties.find(function(p) {
                    return p.name === edited.name;
                });
                if (property) {
                    edited.value = property.value;
                }
                if (ctrl.newProperties[ctrl.newProperties.length - 1].name) {
                    ctrl.newProperties.push({
                        name : null,
                        value : null
                    });
                }
            };

            ctrl.updateEnvironment = function() {
                ctrl.error = null;
                var newPropertyMap = ctrl.newProperties.reduce(function(map, property) {
                    if (property.name) {
                        map[property.name] = property.value;
                    }
                    return map;
                }, {});
                ctrl.application.setEnv(newPropertyMap).then(function() {
                    ctrl.envChangedCallback()();
                }).catch(function(error) {
                    ctrl.error = error;
                    ctrl.envChangedCallback()();
                });
            };

            ctrl.resetEnvironment = function() {
                ctrl.error = null;
                ctrl.application.resetEnv().then(function() {
                    ctrl.envChangedCallback()();
                }).catch(function(error) {
                    ctrl.error = error;
                    ctrl.envChangedCallback()();
                });
            };

            ctrl.refreshContext = function() {
                ctrl.application.refresh().then(function() {
                    ctrl.envChangedCallback()();
                }).catch(function(error) {
                    ctrl.error = error;
                    ctrl.envChangedCallback()();
                });
            };
        },
        template : 
                '<datalist id="allPropertyNames">'
                + '	<option ng-repeat="name in $ctrl.allPropertyNames | orderBy:\'toString()\'">{{name}}</option>'
                + '</datalist>'
                + '<table class="table">'
                + '	<col width="38%">'
                + '	<col width="62%">'
                + '	<tr ng-repeat="property in $ctrl.newProperties"> '
                + '		<td><input type="text" class="input-xlarge" list="allPropertyNames" placeholder="property name" ng-model="property.name" ng-change="$ctrl.onChangePropertyName(property)"></input></td> '
                + '		<td><input type="text" class="input-xxlarge" placeholder="property value" ng-model="property.value"></input></td> '
                + '	</tr>'
                + '	<tr class="error" ng-if="$ctrl.error">'
                + '		<td colspan="2"><b>Error:</b> {{$ctrl.error}}</td>'
                + '	</tr>'
                + '	<tr><td colspan="2">'
                + '	        <div class="control-group pull-left">'
                + '	                <button class="btn btn-info" ng-click="$ctrl.refreshContext()">refresh context</button>'
                + '	        </div>'
                + '	        <div class="control-group pull-right">'
                + '	                <button class="btn btn-warning" ng-click="$ctrl.updateEnvironment()">update environment</button>'
                + '	                <button class="btn" ng-click="$ctrl.resetEnvironment()">reset environment</button>'
                + '	        </div>'
                + '	</td></tr>'
                + '</table>'
    };
