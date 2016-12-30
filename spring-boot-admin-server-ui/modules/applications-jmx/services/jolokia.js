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

var angular = require('angular');
var Jolokia = require('jolokia');

module.exports = function ($q) {
  'ngInject';

  var outer = this;
  var j4p = new Jolokia();

  this.bulkRequest = function (url, requests) {
    var deferred = $q.defer();
    deferred.notify(requests);

    var hasError = false;
    var responses = [];

    j4p.request(requests, {
      url: url,
      method: 'post',
      success: function (response) {
        responses.push(response);
        if (responses.length >= requests.length) {
          if (!hasError) {
            deferred.resolve(responses);
          } else {
            deferred.resolve(responses);
          }
        }
      },
      error: function (response) {
        hasError = true;
        responses.push(response);
        if (responses.length >= requests.length) {
          deferred.reject(responses);
        }
      },
      ajaxError: function (response) {
        hasError = true;
        responses.push(angular.fromJson(response.responseText));
        if (responses.length >= requests.length) {
          deferred.reject(responses, response.status);
        }
      }
    });

    return deferred.promise;
  };

  this.request = function (url, request) {
    var deferred = $q.defer();
    deferred.notify(request);

    j4p.request(request, {
      url: url,
      method: 'post',
      success: function (response) {
        deferred.resolve(response);
      },
      error: function (response) {
        deferred.reject(response);
      },
      ajaxError: function (response) {
        deferred.reject(angular.fromJson(response.responseText), response.status);
      }
    });

    return deferred.promise;
  };

  this.exec = function (url, mbean, op, args) {
    return outer.request(url, {
      type: 'exec',
      mbean: mbean,
      operation: op,
      arguments: args
    });
  };

  this.read = function (url, mbean) {
    return outer.request(url, {
      type: 'read',
      mbean: mbean,
      config: { ignoreErrors: true }
    });
  };

  this.readAttr = function (url, mbean, attr) {
    return outer.request(url, {
      type: 'read',
      mbean: mbean,
      attribute: attr,
      config: { ignoreErrors: true }
    });
  };

  this.writeAttr = function (url, mbean, attr, val) {
    return outer.request(url, {
      type: 'write',
      mbean: mbean,
      attribute: attr,
      value: val
    });
  };

  this.list = function (url) {
    return outer.request(url, {
      type: 'list',
      config: { ignoreErrors: true }
    });
  };

  this.search = function (url, mbean) {
    return outer.request(url, {
      type: 'search',
      mbean: mbean,
      config: { ignoreErrors: true }
    });
  };
};
