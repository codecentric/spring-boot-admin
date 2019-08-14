/*
 * Copyright 2014-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import flatMap from 'lodash/flatMap';
import groupBy from 'lodash/groupBy';
import union from 'lodash/union';

const convertLoggers = function (loggers, instanceId) {
  return Object.entries(loggers)
    .map(([name, config]) => ({name, level: [{...config, instanceId}]}));
};

export class InstanceLoggers {
  constructor(instance) {
    this.instance = instance;
  }

  async fetchLoggers() {
    const loggerConfig = (await this.instance.fetchLoggers()).data;
    return {
      levels: loggerConfig.levels,
      loggers: convertLoggers(loggerConfig.loggers, this.instance.id)
    }
  }

  async configureLogger(name, level) {
    await this.instance.configureLogger(name, level);
  }
}

export class ApplicationLoggers {
  constructor(application) {
    this.application = application;
  }

  async fetchLoggers() {
    const responses = (await this.application.fetchLoggers()).responses;
    return {
      levels: union(...responses.map(r => r.body.levels)),
      loggers: Object.entries(
        groupBy(
          flatMap(responses, r => convertLoggers(r.body.loggers, r.instanceId)),
          l => l.name
        )
      ).map(([name, configs]) => ({name, level: flatMap(configs, c => c.level)}))
    };
  }

  async configureLogger(name, level) {
    await this.application.configureLogger(name, level);
  }
}
