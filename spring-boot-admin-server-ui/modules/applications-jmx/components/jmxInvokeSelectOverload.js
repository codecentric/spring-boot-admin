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
        require : {
            invocation : '^sbaJmxInvocation'
        },
        controller : function() {
            var ctrl = this;
            ctrl.selectOverload = function(operation) {
                ctrl.invocation.descriptor = operation;
                ctrl.invocation.proceedJmxInvocation();
            };
        },
        template : '<form class="form">'
                + '	<p>The method {{$ctrl.invocation.name}} is overloaded. Please choose a variant.</p>'
                + '	<div class="control-group" >'
                + '		<button class="btn btn-block" style="text-align: left; padding: 8px 15px;" ng-repeat="op in $ctrl.invocation.descriptor" ng-click="$ctrl.selectOverload(op)">'
                + '			<b ng-bind="$ctrl.invocation.name"></b>('
                + '				<span ng-repeat-start="arg in op.args" data-toggle="tooltip" title="{{arg.desc}}">{{arg.type}} {{arg.name}}</span><span ng-repeat-end ng-if="!$last">, </span>'
                + '			)<br/>'
                + '			<small class="muted" ng-bind="op.ret"></small>'
                + '			<span class="help-block" ng-bind="op.desc"></span>'
                + '		</button>'
                + '	</div>'
                + '</form>'
    };
