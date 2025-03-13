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
import { AxiosInstance } from 'axios';
import saveAs from 'file-saver';
import { Observable, concat, from, ignoreElements } from 'rxjs';

import axios, {
  redirectOn401,
  registerErrorToastInterceptor,
} from '../utils/axios.js';
import waitForPolyfill from '../utils/eventsource-polyfill';
import logtail from '../utils/logtail';
import uri from '../utils/uri';

import { useSbaConfig } from '@/sba-config';
import { actuatorMimeTypes } from '@/services/spring-mime-types';

const isInstanceActuatorRequest = (url: string) =>
  url.match(/^instances[/][^/]+[/]actuator([/].*)?$/);

class Instance {
  public readonly id: string;
  public registration: Registration;
  public endpoints: any[] = [];
  public tags: { [key: string]: string }[];
  public statusTimestamp: string;
  public buildVersion: string;
  public statusInfo: StatusInfo;
  private readonly axios: AxiosInstance;

  constructor({ id, ...instance }) {
    Object.assign(this, instance);
    this.id = id;
    this.axios = axios.create({
      withCredentials: true,
      baseURL: uri`instances/${this.id}`,
      headers: { Accept: actuatorMimeTypes.join(',') },
    });
    this.axios.interceptors.response.use(
      (response) => response,
      redirectOn401(
        (error) =>
          !isInstanceActuatorRequest(error.config.baseURL + error.config.url),
      ),
    );
    registerErrorToastInterceptor(this.axios);
  }

  get isUnregisterable() {
    return this.registration.source === 'http-api';
  }

  static async fetchEvents() {
    return axios.get(uri`instances/events`, {
      headers: { Accept: 'application/json' },
    });
  }

  static getEventStream() {
    return concat(
      from(waitForPolyfill()).pipe(ignoreElements()),
      Observable.create((observer) => {
        const eventSource = new EventSource('instances/events');
        eventSource.onmessage = (message) =>
          observer.next({
            ...message,
            data: JSON.parse(message.data),
          });
        eventSource.onerror = (err) => observer.error(err);
        return () => {
          eventSource.close();
        };
      }),
    );
  }

  static async get(id) {
    return axios.get(uri`instances/${id}`, {
      headers: { Accept: 'application/json' },
      transformResponse(data) {
        if (!data) {
          return data;
        }
        const instance = JSON.parse(data);
        return new Instance(instance);
      },
    });
  }

  static _toMBeans(data) {
    if (!data) {
      return data;
    }
    const raw = JSON.parse(data);
    return Object.entries(raw.value).map(([domain, mBeans]) => ({
      domain,
      mBeans: Object.entries(mBeans).map(([descriptor, mBean]) => ({
        descriptor: descriptor,
        ...mBean,
      })),
    }));
  }

  showUrl() {
    const sbaConfig = useSbaConfig();
    if (sbaConfig.uiSettings.hideInstanceUrl) {
      return false;
    }

    const hideUrlMetadata = this.registration.metadata?.['hide-url'];
    return hideUrlMetadata !== 'true';
  }

  getId() {
    return this.id;
  }

  hasEndpoint(endpointId) {
    return this.endpoints.some((endpoint) => endpoint.id === endpointId);
  }

  async unregister() {
    return this.axios.delete('', {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchInfo() {
    return this.axios.get(uri`actuator/info`);
  }

  async fetchMetrics() {
    return this.axios.get(uri`actuator/metrics`);
  }

  async fetchMetric(metric, tags) {
    const params = new URLSearchParams();
    if (tags) {
      let firstElementDuplicated = false;
      Object.entries(tags)
        .filter(([, value]) => typeof value !== 'undefined' && value !== null)
        .forEach(([name, value]) => {
          params.append('tag', `${name}:${value}`);

          if (!firstElementDuplicated) {
            // workaround for tags that contains comma
            // take a look at https://github.com/spring-projects/spring-framework/issues/23820#issuecomment-543087878
            // If there is single tag specified and name or value contains comma then it will be incorrectly split into several parts
            // To bypass it we duplicate first tag.
            params.append('tag', `${name}:${value}`);
            firstElementDuplicated = true;
          }
        });
    }
    return this.axios.get(uri`actuator/metrics/${metric}`, {
      params,
    });
  }

  async fetchHealth() {
    return await this.axios.get(uri`actuator/health`, { validateStatus: null });
  }

  async fetchHealthGroup(groupName: string) {
    return await this.axios.get(uri`actuator/health/${groupName}`, {
      validateStatus: null,
    });
  }

  async fetchEnv(name) {
    return this.axios.get(uri`actuator/env/${name || ''}`);
  }

  async fetchConfigprops() {
    return this.axios.get(uri`actuator/configprops`);
  }

  async hasEnvManagerSupport() {
    const response = await this.axios.options(uri`actuator/env`);
    return (
      response.headers['allow'] && response.headers['allow'].includes('POST')
    );
  }

  async resetEnv() {
    return this.axios.delete(uri`actuator/env`);
  }

  async setEnv(name, value) {
    return this.axios.post(
      uri`actuator/env`,
      { name, value },
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );
  }

  async refreshContext() {
    return this.axios.post(uri`actuator/refresh`);
  }

  async busRefreshContext() {
    return this.axios.post(uri`actuator/busrefresh`);
  }

  async fetchLiquibase() {
    return this.axios.get(uri`actuator/liquibase`);
  }

  async fetchScheduledTasks() {
    return this.axios.get(uri`actuator/scheduledtasks`);
  }

  async fetchGatewayGlobalFilters() {
    return this.axios.get(uri`actuator/gateway/globalfilters`);
  }

  async addGatewayRoute(route) {
    return this.axios.post(uri`actuator/gateway/routes/${route.id}`, route, {
      headers: { 'Content-Type': 'application/json' },
    });
  }

  async fetchGatewayRoutes() {
    return this.axios.get(uri`actuator/gateway/routes`);
  }

  async deleteGatewayRoute(routeId) {
    return this.axios.delete(uri`actuator/gateway/routes/${routeId}`);
  }

  async refreshGatewayRoutesCache() {
    return this.axios.post(uri`actuator/gateway/refresh`);
  }

  async fetchCaches() {
    return this.axios.get(uri`actuator/caches`);
  }

  async clearCaches() {
    return this.axios.delete(uri`actuator/caches`);
  }

  async clearCache(name, cacheManager) {
    return this.axios.delete(uri`actuator/caches/${name}`, {
      params: { cacheManager: cacheManager },
    });
  }

  async fetchFlyway() {
    return this.axios.get(uri`actuator/flyway`);
  }

  async fetchLoggers() {
    return this.axios.get(uri`actuator/loggers`);
  }

  async configureLogger(name, level) {
    await this.axios.post(
      uri`actuator/loggers/${name}`,
      level === null ? {} : { configuredLevel: level },
      {
        headers: { 'Content-Type': 'application/json' },
      },
    );
  }

  async fetchHttptrace() {
    return this.axios.get(uri`actuator/httptrace`);
  }

  async fetchHttpExchanges() {
    return this.axios.get(uri`actuator/httpexchanges`);
  }

  async fetchBeans() {
    return this.axios.get(uri`actuator/beans`);
  }

  async fetchConditions() {
    return this.axios.get(uri`actuator/conditions`);
  }

  async fetchThreaddump() {
    return this.axios.get(uri`actuator/threaddump`);
  }

  async downloadThreaddump() {
    const res = await this.axios.get(uri`actuator/threaddump`, {
      headers: { Accept: 'text/plain' },
    });
    const blob = new Blob([res.data], { type: 'text/plain;charset=utf-8' });
    saveAs(blob, this.registration.name + '-threaddump.txt');
  }

  async fetchAuditevents({ after, type, principal }) {
    return this.axios.get(uri`actuator/auditevents`, {
      params: {
        after: after.toISOString(),
        type: type || undefined,
        principal: principal || undefined,
      },
    });
  }

  async fetchSessionsByUsername(username) {
    return this.axios.get(uri`actuator/sessions`, {
      params: {
        username: username || undefined,
      },
    });
  }

  async fetchSession(sessionId) {
    return this.axios.get(uri`actuator/sessions/${sessionId}`);
  }

  async deleteSession(sessionId) {
    return this.axios.delete(uri`actuator/sessions/${sessionId}`);
  }

  async fetchStartup() {
    const optionsResponse = await this.axios.options(uri`actuator/startup`);
    if (
      optionsResponse.headers.allow &&
      optionsResponse.headers.allow.includes('GET')
    ) {
      return this.axios.get(uri`actuator/startup`);
    }

    return this.axios.post(uri`actuator/startup`);
  }

  streamLogfile(interval) {
    return logtail(
      (opt) => this.axios.get(uri`actuator/logfile`, opt),
      interval,
    );
  }

  async listMBeans() {
    return this.axios.get(uri`actuator/jolokia/list`, {
      headers: { Accept: 'application/json' },
      params: { canonicalNaming: false },
      transformResponse: Instance._toMBeans,
    });
  }

  async readMBeanAttributes(domain, mBean) {
    const body = {
      type: 'read',
      mbean: `${domain}:${mBean}`,
      config: { ignoreErrors: true },
    };
    return this.axios.post(uri`actuator/jolokia`, body, {
      headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
      },
    });
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

  async fetchMappings() {
    return this.axios.get(uri`actuator/mappings`);
  }

  async fetchQuartzJobs() {
    return this.axios.get(uri`actuator/quartz/jobs`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchQuartzJob(group, name) {
    return this.axios.get(uri`actuator/quartz/jobs/${group}/${name}`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchQuartzTriggers() {
    return this.axios.get(uri`actuator/quartz/triggers`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchQuartzTrigger(group, name) {
    return this.axios.get(uri`actuator/quartz/triggers/${group}/${name}`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchSbomIds() {
    return this.axios.get(uri`actuator/sbom`, {
      headers: { Accept: 'application/json' },
    });
  }

  async fetchSbom(id) {
    return this.axios.get(uri`actuator/sbom/${id}`, {
      headers: { Accept: '*/*' },
    });
  }

  shutdown() {
    return this.axios.post(uri`actuator/shutdown`);
  }

  restart() {
    return this.axios.post(uri`actuator/restart`);
  }
}

export default Instance;

export type Registration = {
  name: string;
  managementUrl?: string;
  healthUrl: string;
  serviceUrl?: string;
  source: string;
  metadata?: { [key: string]: string }[];
};

type StatusInfo = {
  status:
    | 'UNKNOWN'
    | 'OUT_OF_SERVICE'
    | 'UP'
    | 'DOWN'
    | 'OFFLINE'
    | 'RESTRICTED'
    | string;
  details: { [key: string]: string };
};

export const DOWN_STATES = ['OUT_OF_SERVICE', 'DOWN', 'OFFLINE', 'RESTRICTED'];
export const UP_STATES = ['UP'];
export const UNKNOWN_STATES = ['UNKNOWN'];
