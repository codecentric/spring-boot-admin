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
            ctrl.$onInit = function() {
                ctrl.arguments = new Array(ctrl.invocation.descriptor.args.length);
            };
            ctrl.applyParameters = function() {
                ctrl.invocation.arguments = ctrl.arguments;
                ctrl.invocation.proceedJmxInvocation();
            };
        },
        template : '<form class="form">'
                + '     <p>Please input the arguments for {{$ctrl.invocation.signature}} <small class="muted">: {{$ctrl.invocation.descriptor.ret}}</small></p>'
                + '	<div class="control-group" ng-repeat="arg in $ctrl.invocation.descriptor.args">'
                + '		<label class="control-label" for="{{arg.name}}"	style="word-break: break-all;">'
                + '			{{arg.name}} <small class="muted" style="word-break: break-all;" ng-bind="arg.type"></small>'
                + '		</label>'
                + '		<div class="controls">'
                + '			<sba-java-type-input type="{{arg.type}}" decription="{{arg.desc}}" model="$ctrl.arguments[$index]"> '
                + '			</sba-java-type-input>'
                + '		</div>'
                + '	</div>'
                + '	<button class="btn btn-primary" ng-click="$ctrl.applyParameters()">Execute</button>'
                + '</form>'
    };
