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
            bean : '<bean',
            application : '<application'
        },
        controller : function(ApplicationJmx) {
            var ctrl = this;
            ctrl.attributeValues = {};
            ctrl.attributeErrors = {};
            ctrl.error = null;
            ctrl.invocation = null;

            ctrl.readAttributeValues = function() {
                ctrl.readingAttributes = true;
                ctrl.error = null;
                ctrl.attributeErrors = {};
                ctrl.attributeValues = {};
                ApplicationJmx.readAllAttr(ctrl.application, ctrl.bean).then(
                        function(response) {
                            ctrl.attributeValues = response.value;
                            ctrl.readingAttributes = false;
                        }).catch(function(response) {
                            ctrl.error = response.error;
                            ctrl.readingAttributes = false;
                        });
            };

            ctrl.writeAttributeValue = function(name, value) {
                ctrl.attributeErrors[name] = null;
                ApplicationJmx.writeAttr(ctrl.application, ctrl.bean, name, value).catch(
                        function(response) {
                            ctrl.attributeErrors[name] = response.error;
                        });
            };

            ctrl.doInvoke = function(name, operation) {
                ctrl.invocation = {
                    name : name,
                    operation : operation
                };
            };

            ctrl.cancelInvoke = function() {
                ctrl.invocation = null;
            };
        },
        template : '<h3 ng-bind="$ctrl.bean.name"><br/><small ng-bind="$ctrl.bean.description"></small></h3>'
                + '<div ng-show="error" class="alert alert-error">'
                + ' 	<b>Error:</b> {{$ctrl.error}}'
                + '</div>'
                + '<dl>'
                + '     <dt>Id</dt>'
                + '     <dd style="word-break: break-all;" ng-bind="$ctrl.bean.id"></dd>'
                + '     <dt ng-repeat-start="(name, value) in $ctrl.bean.nameProps" ng-bind="name"></dt>'
                + '     <dd ng-repeat-end ng-bind="value"></dd>'
                + '</dl>'
                + '<form class="form-horizontal" >'
                + '<fieldset ng-show="$ctrl.bean.attributes">'
                + '    <legend>Attributes <button class="btn" ng-click="$ctrl.readAttributeValues()">read</button><span ng-show="$ctrl.readingAttributes" class="refresh"></span></legend>'
                + '    <div class="control-group" ng-repeat="(name, attribute) in $ctrl.bean.attributes track by name" ng-class="{error: $ctrl.attributeErrors[attribute.name]}">'
                + '        <label class="control-label" for="{{name}}" style="word-break: break-all;">'
                + '           {{name}}<br/>'
                + '           <small class="muted" ng-bind="attribute.type"></small>'
                + '	   </label>'
                + '        <div class="controls">'
                + '       	<div class="input-prepend">'
                + '           	    <button class="btn" type="button" ng-click="$ctrl.writeAttributeValue(name, $ctrl.attributeValues[name])" ng-disabled="!attribute.rw">write</button>'
                + '		    <sba-java-type-input type="{{attribute.type}}" model="$ctrl.attributeValues[name]" disabled="!attribute.rw"></sba-java-type-input>'
                + '             </div>'
                + '             <span class="help-block" ng-bind="attribute.desc"></span>'
                + '             <span class="help-inline" ng-bind="$ctrl.attributeErrors[name]"></span>'
                + '   	   </div>'
                + '	</div>'
                + '</fieldset>'
                + '<fieldset ng-show="$ctrl.bean.operations">'
                + '	<legend>Operations</legend>'
                + '     <div class="control-group" ng-if="!$ctrl.invocation">'
                + '   	    <button ng-repeat="(name, op) in $ctrl.bean.operations track by name" class="btn btn-block" style="text-align: left; padding: 8px 15px;" ng-click="$ctrl.doInvoke(name, op)">'
                + '			<b ng-bind="name"></b>'
                + '			<span ng-if="!op.length">('
                + '				<span ng-repeat-start="arg in op.args" data-toggle="tooltip" title="{{arg.desc}}">{{arg.type}} {{arg.name}}</span><span ng-repeat-end ng-if="!$last">, </span>'
                + '			)</span><br/>'
                + '			<span class="help-block"><small class="muted" style="word-break: break-all;" ng-bind="op.ret"></small></span>'
                + '			<span class="help-block"><small class="muted" ng-show="op.length > 0">overloaded</small></span>'
                + '           <span class="help-block" ng-bind="op.desc"></span>'
                + '   	</button>'
                + '	</div>'
                + '	<div ng-if="$ctrl.invocation" >'
                + '		<sba-jmx-invocation operation-name="$ctrl.invocation.name" operation-descriptor="$ctrl.invocation.operation" on-cancel="$ctrl.cancelInvoke()" /></sba-jmx-invocation>'
                + '	</div> '
                + '</fieldset>'
                + '</form>'
    };
