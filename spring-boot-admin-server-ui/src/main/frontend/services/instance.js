/*
 * Copyright 2014-2018 the original author or authors.
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

import axios, {redirectOn401} from '@/utils/axios';
import waitForPolyfill from '@/utils/eventsource-polyfill';
import logtail from '@/utils/logtail';
import {concat, from, ignoreElements, Observable} from '@/utils/rxjs';
import uri from '@/utils/uri';
import _ from 'lodash';

const actuatorMimeTypes = [
  'application/vnd.spring-boot.actuator.v2+json',
  'application/vnd.spring-boot.actuator.v1+json',
  'application/json'
];

const isInstanceActuatorRequest = url => url.match(/^instances[/][^/]+[/]actuator([/].*)?$/);

class Instance {
  constructor(id) {
    this.id = id;
    this.axios = axios.create({
      baseURL: uri`instances/${this.id}/`,
    });
    this.axios.interceptors.response.use(
      response => response,
      redirectOn401(error => !isInstanceActuatorRequest(error.config.url))
    );
  }

  hasEndpoint(endpointId) {
    return this.endpoints.findIndex(endpoint => endpoint.id === endpointId) >= 0;
  }

  get isUnregisterable() {
    return this.registration.source === 'http-api';
  }

  async unregister() {
    return this.axios.delete('');
  }

  async fetchInfo() {
    return this.axios.get(uri`actuator/info`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchMetrics() {
    return this.axios.get(uri`actuator/metrics`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchMetric(metric, tags) {
    const params = tags ? {
      tag: _.entries(tags)
        .filter(([, value]) => typeof value !== 'undefined' && value !== null)
        .map(([name, value]) => `${name}:${value}`)
        .join(',')
    } : {};
    return this.axios.get(uri`actuator/metrics/${metric}`, {
      headers: {'Accept': actuatorMimeTypes},
      params
    });
  }

  async fetchHealth() {
    try {
      return await this.axios.get(uri`actuator/health`, {
        headers: {'Accept': actuatorMimeTypes}
      });
    } catch (error) {
      if (error.response) {
        return error.response;
      }
      throw error;
    }
  }

  async fetchEnv(name) {
    return this.axios.get(uri`actuator/env/${name || '' }`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchConfigprops() {
    return this.axios.get(uri`actuator/configprops`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async hasEnvManagerSupport() {
    const response = await this.axios.options(uri`actuator/env`);
    return response.headers['allow'] && response.headers['allow'].includes('POST');
  }

  async resetEnv() {
    return this.axios.delete(uri`actuator/env`);
  }

  async setEnv(name, value) {
    return this.axios.post(uri`actuator/env`, {name, value}, {
      headers: {'Content-Type': 'application/json'}
    });
  }

  async refreshContext() {
    return this.axios.post(uri`actuator/refresh`);
  }

  async fetchLiquibase() {
    return this.axios.get(uri`actuator/liquibase`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchScheduledTasks() {
    return this.axios.get(uri`actuator/scheduledtasks`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchFlyway() {
    return this.axios.get(uri`actuator/flyway`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchLoggers() {
    return this.axios.get(uri`actuator/loggers`, {
      headers: {'Accept': actuatorMimeTypes},
      transformResponse: Instance._toLoggers
    });
  }

  async configureLogger(name, level) {
    return this.axios.post(uri`actuator/loggers/${name}`, {configuredLevel: level}, {
      headers: {'Content-Type': 'application/json'}
    });
  }

  async fetchHttptrace() {
    return this.axios.get(uri`actuator/httptrace`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchThreaddump() {
    return this.axios.get(uri`actuator/threaddump`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchAuditevents(after) {
    return this.axios.get(uri`actuator/auditevents`, {
      headers: {'Accept': actuatorMimeTypes},
      params: {after: after.toISOString()}
    });
  }

  async fetchSessionsByUsername(username) {
    return this.axios.get(uri`actuator/sessions`, {
      headers: {'Accept': actuatorMimeTypes},
      params: {username}
    });
  }

  async fetchSession(sessionId) {
    return this.axios.get(uri`actuator/sessions/${sessionId}`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async deleteSession(sessionId) {
    return this.axios.delete(uri`actuator/sessions/${sessionId}`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  streamLogfile(interval) {
    return logtail(opt => this.axios.get(uri`actuator/logfile`, opt), interval);
  }

  async listMBeans() {
    return this.axios.get(uri`actuator/jolokia/list`, {
      headers: {'Accept': 'application/json'},
      params: {canonicalNaming: false},
      transformResponse: Instance._toMBeans
    });
  }

  async readMBeanAttributes(domain, mBean) {
    const body = {
      type: 'read',
      mbean: `${domain}:${mBean}`,
      config: {ignoreErrors: true}
    };
    return this.axios.post(uri`actuator/jolokia`, body, {
      headers: {'Accept': 'application/json', 'Content-Type': 'application/json'}
    });
  }

  async writeMBeanAttribute(domain, mBean, attribute, value) {
    const body = {
      type: 'write',
      mbean: `${domain}:${mBean}`,
      attribute,
      value
    };
    return this.axios.post(uri`actuator/jolokia`, body, {
      headers: {'Accept': 'application/json', 'Content-Type': 'application/json'}
    });
  }

  async invokeMBeanOperation(domain, mBean, operation, args) {
    const body = {
      type: 'exec',
      mbean: `${domain}:${mBean}`,
      operation,
      'arguments': args
    };
    return this.axios.post(uri`actuator/jolokia`, body, {
      headers: {'Accept': 'application/json', 'Content-Type': 'application/json'}
    });
  }

  async fetchMappings() {
    return this.axios.get(uri`actuator/mappings`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  static async fetchEvents() {
    return axios.get('instances/events');
  }

  static getEventStream() {
    return concat(
      from(waitForPolyfill()).pipe(ignoreElements()),
      Observable.create(observer => {
        const eventSource = new EventSource('instances/events');
        eventSource.onmessage = message => observer.next({
          ...message,
          data: JSON.parse(message.data)
        });
        eventSource.onerror = err => observer.error(err);
        return () => {
          eventSource.close();
        };
      })
    );
  }

  static async get(id) {
    return axios.get(uri`instances/${id}`, {
      transformResponse: Instance._toInstance
    });
  }

  static _toInstance(data) {
    if (!data) {
      return data;
    }
    const instance = JSON.parse(data);
    return Object.assign(new Instance(instance.id), instance);
  }

  static _toLoggers(data) {
    if (!data) {
      return data;
    }
    const raw = JSON.parse(data);
    const loggers = _.transform(raw.loggers, (result, value, key) => {
      return result.push({name: key, ...value});
    }, []);
    return {levels: raw.levels, loggers};
  }

  static _toMBeans(data) {
    if (!data) {
      return data;
    }
    const raw = JSON.parse(data);
    return _.entries(raw.value).map(([domain, mBeans]) => ({
      domain,
      mBeans: _.entries(mBeans).map(([descriptor, mBean]) => ({
        descriptor: descriptor,
        ...mBean
      }))
    }))
  }
}

export default Instance;
