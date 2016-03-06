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
        template : '<div>'
                + '	<p>Result for {{$ctrl.invocation.call}} <small class="muted">: {{$ctrl.invocation.descriptor.ret}}</small></p>'
                + '	<div ng-if="!$ctrl.invocation.response" class="progress progress-striped active">'
                + '		<div class="bar" style="width: 100%;">executing ... </div>'
                + '	</div>'
                + '	<div ng-if="$ctrl.invocation.response.value !== undefined" >'
                + '		<div class="alert alert-success"><b>Success</b></div>'
                + '		<pre style="white-space: pre-wrap;">{{ $ctrl.invocation.response.value | json }}</pre>'
                + '	</div>'
                + '	<div ng-if="$ctrl.invocation.response.error">'
                + '		<div class="alert alert-error">'
                + '			<b>Error:</b> {{$ctrl.invocation.response.error}}'
                + '		</div>'
                + '		<pre style="white-space: pre-wrap;">{{$ctrl.invocation.response.stacktrace}}</pre>'
                + '	</div>'
                + '</div>'
    };
