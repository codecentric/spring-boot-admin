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
import { sortBy } from 'lodash-es';

import axios, { redirectOn401 } from '../utils/axios';
import waitForPolyfill from '../utils/eventsource-polyfill';
import { Observable, concat, from, ignoreElements } from '../utils/rxjs.js';
import uri from '../utils/uri';
import Instance from './instance';

const actuatorMimeTypes = [
  'application/vnd.spring-boot.actuator.v2+json',
  'application/vnd.spring-boot.actuator.v1+json',
  'application/json',
];

export const hasMatchingContentType = (contentType, compatibleContentTypes) =>
  Boolean(contentType) &&
  compatibleContentTypes.includes(contentType.replace(/;.*$/, ''));

export const convertBody = (responses) =>
  responses.map((res) => {
    if (
      res.body &&
      hasMatchingContentType(res.contentType, actuatorMimeTypes)
    ) {
      return {
        ...res,
        body: JSON.parse(res.body),
      };
    }
    return res;
  });

class Application {
  constructor({ name, instances, ...application }) {
    Object.assign(this, application);
    this.name = name;
    this.axios = axios.create({
      withCredentials: true,
      baseURL: uri`applications/${this.name}`,
      headers: {
        'X-SBA-REQUEST': true,
      },
    });
    this.axios.interceptors.response.use(
      (response) => response,
      redirectOn401()
    );
    this.instances = sortBy(
      instances.map(
        (i) => new Instance(i),
        [(instance) => instance.registration.healthUrl]
      )
    );
  }

  filterInstances(predicate) {
    return new Application({
      ...this,
      instances: this.instances.filter(predicate),
    });
  }

  hasEndpoint(endpointId) {
    return this.instances.some((i) => i.hasEndpoint(endpointId));
  }

  findInstance(instanceId) {
    return this.instances.find((instance) => instance.id === instanceId);
  }

  get isUnregisterable() {
    return this.instances.some((i) => i.isUnregisterable);
  }

  get hasShutdownEndpoint() {
    return this.hasEndpoint('shutdown');
  }

  get hasRestartEndpoint() {
    return this.hasEndpoint('restart');
  }

  async unregister() {
    return this.axios.delete('', {
      headers: { Accept: 'application/json' },
    });
  }

  static async list() {
    return axios.get('applications', {
      headers: { Accept: 'application/json', 'X-SBA-REQUEST': true },
      transformResponse: Application._transformResponse,
    });
  }

  static getStream() {
    return concat(
      from(waitForPolyfill()).pipe(ignoreElements()),
      Observable.create((observer) => {
        const eventSource = new EventSource('applications');
        eventSource.onmessage = (message) =>
          observer.next({
            ...message,
            data: Application._transformResponse(message.data),
          });

        eventSource.onerror = (err) => observer.error(err);
        return () => eventSource.close();
      })
    );
  }

  async fetchLoggers() {
    const responses = convertBody(
      (
        await this.axios.get(uri`actuator/loggers`, {
          headers: { Accept: actuatorMimeTypes.join(',') },
        })
      ).data
    );
    return { responses };
  }

  async configureLogger(name, level) {
    const responses = (
      await this.axios.post(
        uri`actuator/loggers/${name}`,
        level === null ? {} : { configuredLevel: level },
        { headers: { 'Content-Type': 'application/json' } }
      )
    ).data;
    return { responses };
  }

  async setEnv(name, value) {
    return this.axios.post(
      uri`actuator/env`,
      { name, value },
      {
        headers: { 'Content-Type': 'application/json' },
      }
    );
  }

  async resetEnv() {
    return this.axios.delete(uri`actuator/env`);
  }

  async refreshContext() {
    return this.axios.post(uri`actuator/refresh`);
  }

  async clearCaches() {
    return this.axios.delete(uri`actuator/caches`);
  }

  shutdown() {
    return this.axios.post(uri`actuator/shutdown`);
  }

  restart() {
    return this.axios.post(uri`actuator/restart`);
  }

  async writeMBeanAttribute(domain, mBean, attribute, value) {
    const body = {
      type: 'write',
      mbean: `${domain}:${mBean}`,
      attribute,
      value,
    };
    return this.axios.post(uri`actuator/jolokia`, body, {
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    });
  }

  async invokeMBeanOperation(domain, mBean, operation, args) {
    const body = {
      type: 'exec',
      mbean: `${domain}:${mBean}`,
      operation,
      arguments: args,
    };
    return this.axios.post(uri`actuator/jolokia`, body, {
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    });
  }

  static _transformResponse(data) {
    if (!data) {
      return data;
    }
    const json = JSON.parse(data);
    if (json instanceof Array) {
      const applications = json.map((j) => new Application(j));
      return sortBy(applications, [(item) => item.name]);
    }
    return new Application(json);
  }
}

export default Application;
