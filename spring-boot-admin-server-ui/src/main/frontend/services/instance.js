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

import axios from '@/utils/axios';
import waitForPolyfill from '@/utils/eventsource-polyfill';
import logtail from '@/utils/logtail';
import {Observable} from '@/utils/rxjs'
import _ from 'lodash';

const actuatorMimeTypes = ['application/vnd.spring-boot.actuator.v2+json',
  'application/vnd.spring-boot.actuator.v1+json',
  'application/json'];

class Instance {
  constructor(id) {
    this.id = id;
  }

  hasEndpoint(endpointId) {
    return this.endpoints.findIndex(endpoint => endpoint.id === endpointId) >= 0;
  }

  get isUnregisterable() {
    return this.registration.source === 'http-api';
  }

  async unregister() {
    return axios.delete(`instances/${this.id}`);
  }

  async fetchInfo() {
    return axios.get(`instances/${this.id}/actuator/info`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchMetrics() {
    return axios.get(`instances/${this.id}/actuator/metrics`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }
  async fetchMetric(metric, tags) {
    const params = tags ? {tag: _.entries(tags).map(([name, value]) => `${name}:${value}`).join(',')} : {};
    return axios.get(`instances/${this.id}/actuator/metrics/${metric}`, {
      headers: {'Accept': actuatorMimeTypes},
      params
    });
  }

  async fetchHealth() {
    return axios.get(`instances/${this.id}/actuator/health`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchEnv(name) {
    return await axios.get(`instances/${this.id}/actuator/env${name ? `/${name}` : '' }`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchLiquibase() {
    return await axios.get(`instances/${this.id}/actuator/liquibase`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchFlyway() {
    return await axios.get(`instances/${this.id}/actuator/flyway`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchLoggers() {
    return await axios.get(`instances/${this.id}/actuator/loggers`, {
      headers: {
        'Accept': actuatorMimeTypes
      },
      transformResponse: Instance._toLoggers
    });
  }

  async configureLogger(name, level) {
    return await axios.post(`instances/${this.id}/actuator/loggers/${name}`, {configuredLevel: level}, {
      headers: {
        'Content-Type': 'application/json'
      }
    });
  }

  async fetchHttptrace() {
    return await axios.get(`instances/${this.id}/actuator/httptrace`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchThreaddump() {
    return await axios.get(`instances/${this.id}/actuator/threaddump`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async fetchAuditevents(after) {
    return await axios.get(`instances/${this.id}/actuator/auditevents`, {
      headers: {'Accept': actuatorMimeTypes},
      params: {
        after: after.toISOString()
      }
    });
  }

  async fetchSessions(username) {
    return await axios.get(`instances/${this.id}/actuator/sessions`, {
      headers: {'Accept': actuatorMimeTypes},
      params: {
        username
      }
    });
  }

  async fetchSession(sessionId) {
    return await axios.get(`instances/${this.id}/actuator/sessions/${sessionId}`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  async deleteSession(sessionId) {
    return await axios.delete(`instances/${this.id}/actuator/sessions/${sessionId}`, {
      headers: {'Accept': actuatorMimeTypes}
    });
  }

  streamLogfile(interval) {
    return logtail(`instances/${this.id}/actuator/logfile`, interval);
  }

  static async fetchEvents() {
    return await axios.get(`instances/events`);
  }

  static getEventStream() {
    return Observable.from(waitForPolyfill()).ignoreElements().concat(Observable.create(observer => {
      const eventSource = new EventSource('instances/events');
      eventSource.onmessage = message => observer.next({
        ...message,
        data: JSON.parse(message.data)
      });
      eventSource.onerror = err => observer.error(err);
      return () => {
        eventSource.close();
      };
    }));
  }

  static async get(id) {
    return await axios.get(`instances/${id}`, {
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

}

export default Instance;