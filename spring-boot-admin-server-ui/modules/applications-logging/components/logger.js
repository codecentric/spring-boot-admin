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
  bindings: {
    logger: '<value',
    levelchangeCallback: '&onLevelChanged'
  },
  controller: function () {
    var ctrl = this;

    ctrl.getLoggerClass = function (level) {
      if (ctrl.logger.level !== level) {
        return '';
      }
      switch (ctrl.logger.level) {
        case 'TRACE':
          return 'active btn-danger';
        case 'DEBUG':
          return 'active btn-warning';
        case 'INFO':
          return 'active btn-info';
        case 'WARN':
          return 'active btn-success';
        case 'ERROR':
          return 'active btn-primary';
        case 'OFF':
          return 'active btn-inverse';
        default:
          return '';
      }
    };

    ctrl.setLevel = function (level) {
      ctrl.logger.setLevel(level).finally(function () {
        ctrl.levelchangeCallback()(ctrl.logger, level);
      });
    };
  },
  template: require('./logger.tpl.html')
};
