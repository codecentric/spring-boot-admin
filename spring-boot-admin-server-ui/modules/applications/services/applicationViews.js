/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
'use strict';

module.exports = function($state, $q) {
    var views = [];

    this.register = function(view) {
        views.push(view);
    };

    this.getApplicationViews = function(application) {
        var applicationViews = [];

        views.forEach(function(view) {
            $q.when(!view.show || view.show(application)).then(function(result) {
                if (result) {
                    view.href = $state.href(view.state, {
                        id : application.id
                    });

                    applicationViews.push(view);
                    applicationViews.sort(function(v1, v2) {
                        return (v1.order || 0) - (v2.order || 0);
                    });
                }
            });
        });

        return applicationViews;
    };
};
