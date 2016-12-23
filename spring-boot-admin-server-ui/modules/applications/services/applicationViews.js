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

module.exports = function ($state, $q) {
  'ngInject';

  var views = [];
  this.register = function (view) {
    views.push(view);
    views.sort(function (v1, v2) {
      return (v1.order || 0) - (v2.order || 0);
    });
  };

  var instantiateView = function (view, application) {
    var appView = {
      order: view.order,
      show: view.show || true,
      title: view.title,
      state: view.state
    };

    if (view.state) {
      appView.href = $state.href(view.state, {
        id: application.id
      });
    } else {
      appView.href = view.href.replace('{id}', application.id);
      appView.target = '_blank';
    }

    return appView;
  };

  this.getApplicationViews = function (application) {
    var result = views.map(function (view) {
      return instantiateView(view, application);
    });
    var resolveDynamicViews = function () {
      var deferred = $q.defer();
      result.forEach(function (view) {
        if (typeof view.show === 'function') {
          $q.when(view.show(application)).then(function (result) {
            view.show = result;
            deferred.notify(view);
          });
          return deferred.promise;
        }
      });
    };

    return { views: result, resolve: resolveDynamicViews };
  };
};
