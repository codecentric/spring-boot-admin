/*
 * Copyright 2014-2020 the original author or authors.
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
import { flatMap, groupBy, union } from 'lodash-es';

import Instance from '@/services/instance';

const convertLoggers = function (loggers, instanceId) {
  return Object.entries(loggers).map(([name, config]) => ({
    name,
    level: [{ ...config, instanceId }],
  }));
};

export class InstanceLoggers {
  private readonly instance: Instance;

  constructor(instance: Instance) {
    this.instance = instance;
  }

  async fetchLoggers() {
    const { data: loggerConfig } = await this.instance.fetchLoggers();
    return {
      errors: [],
      levels: loggerConfig.levels,
      loggers: convertLoggers(loggerConfig.loggers, this.instance.id),
      groups: convertLoggers(loggerConfig.groups, this.instance.id),
    };
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
    let errors;
    let levels;
    let loggers;

    try {
      const { responses } = await this.application.fetchLoggers();
      const successful = responses.filter(
        (r) => r.body && r.status >= 200 && r.status < 299,
      );

      errors = responses
        .filter((r) => r.status >= 400)
        .map((r) => ({
          instanceId: r.instanceId,
          error: 'HTTP Status ' + r.status,
        }));
      loggers = Object.entries(
        groupBy(
          flatMap(successful, (r) =>
            convertLoggers(r.body.loggers, r.instanceId),
          ),
          (l) => l.name,
        ),
      ).map(([name, configs]) => ({
        name,
        level: flatMap(configs, (c) => c.level),
      }));
      levels = union(...successful.map((r) => r.body.levels));
    } catch {
      console.warn('Failed to fetch loggers for some instances');

      errors = [];
      levels = [];
      loggers = [];
    }

    return {
      errors,
      levels,
      loggers,
    };
  }

  async configureLogger(name, level) {
    let responses;

    try {
      responses = (await this.application.configureLogger(name, level))
        .responses;
    } catch {
      responses = [];
    }

    const errors = responses
      .filter((r) => r.status >= 400)
      .map((r) => ({
        instanceId: r.instanceId,
        error: 'HTTP Status ' + r.status,
      }));
    if (errors.length > 0) {
      console.warn('Failed to set loglevel for some instances', errors);
    }
  }
}
