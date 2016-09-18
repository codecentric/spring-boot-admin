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

module.exports = function () {
  var granted = false;
  if ('Notification' in window) {
    granted = (window.Notification.permission === 'granted');

    if (!granted && window.Notification.permission !== 'denied') {
      window.Notification.requestPermission(function (permission) {
        granted = (permission === 'granted');
      });
    }
  }

  this.notify = function (title, options) {
    if (granted) {
      var note = new window.Notification(title, options);
      if (options.url !== null) {
        note.onclick = function () {
          window.focus();
          window.open(options.url, '_self');
        };
      }
      if (options.timeout > 0) {
        note.onshow = function () {
          setTimeout(function () {
            note.close();
          }, options.timeout);
        };
      }
    }
  };
};
