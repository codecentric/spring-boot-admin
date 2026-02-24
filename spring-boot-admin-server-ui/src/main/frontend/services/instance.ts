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
} from '../utils/axios';
import waitForPolyfill from '../utils/eventsource-polyfill';
import logtail from '../utils/logtail';
import uri from '../utils/uri';

import { useSbaConfig } from '@/sba-config';
import { actuatorMimeTypes } from '@/services/spring-mime-types';
import { transformToJSON } from '@/utils/transformToJSON';

const isInstanceActuatorRequest = (url: string) =>
  url.match(/^instances[/][^/]+[/]actuator([/].*)?$/);

class Instance {
  public readonly id: string;
  private readonly axios: AxiosInstance;
  public registration: Registration;
  public endpoints: Endpoint[] = [];
  public availableMetrics: string[] = [];
  public tags: { [key: string]: string }[];
  public statusTimestamp: string;
  public buildVersion: string;
  public statusInfo: StatusInfo;

  constructor({ id, ...instance }: InstanceData) {
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

  get metadata() {
    return this.registration.metadata;
  }

  get metadataParsed() {
    const metadata = this.registration.metadata || {};
    return transformToJSON(metadata, 'LAX');
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

  static async get(id: string) {
    return axios.get(uri`instances/${id}`, {
      headers: { Accept: 'application/json' },
      transformResponse(data: string) {
        if (!data) {
          return data;
        }
        const instance = JSON.parse(data);
        return new Instance(instance);
      },
    });
  }

  private static _toMBeans(data: string) {
    if (!data) {
      return data;
    }
    const raw = JSON.parse(data);
    return Object.entries(raw.value).map(([domain, mBeans]) => ({
      domain,
      mBeans: Object.entries(mBeans as Record<string, any>).map(
        ([descriptor, mBean]) => ({
          descriptor: descriptor,
          ...mBean,
        }),
      ),
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

  isUrlDisabled() {
    const sbaConfig = useSbaConfig();
    if (sbaConfig.uiSettings.disableInstanceUrl) {
      return true;
    }

    const disableUrl = this.registration.metadata?.['disable-url'];
    return disableUrl === 'true';
  }

  hasEndpoint(endpointId: string): boolean {
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
    const response = await this.axios.get(uri`actuator/metrics`);
    this.availableMetrics = response?.data?.names ?? [];
    return response;
  }

  async fetchMetric(metric: string, tags: Record<string, any>) {
    if (this.availableMetrics.length === 0) {
      try {
        await this.fetchMetrics();
      } catch (e) {
        console.error('Available metrics could not be determined.', e);
      }
    }

    if (!this.availableMetrics.includes(metric)) {
      console.warn(
        `Metric '${metric}' seems not to be available on instance '${this.id}'.`,
      );
      return;
    }

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
    return await this.axios.get(uri`actuator/health`, {
      validateStatus: null,
    });
  }

  async fetchHealthGroup(groupName: string) {
    return await this.axios.get(uri`actuator/health/${groupName}`, {
      validateStatus: null,
    });
  }

  async fetchEnv(name?: string) {
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

  async setEnv(name: string, value: string) {
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

  async addGatewayRoute(route: { id: string; [key: string]: any }) {
    return this.axios.post(uri`actuator/gateway/routes/${route.id}`, route, {
      headers: { 'Content-Type': 'application/json' },
    });
  }

  async fetchGatewayRoutes() {
    return this.axios.get(uri`actuator/gateway/routes`);
  }

  async deleteGatewayRoute(routeId: string) {
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

  async clearCache(name: string, cacheManager?: string) {
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

  async configureLogger(name: string, level: string | null) {
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

  async fetchAuditevents({
    after,
    type,
    principal,
  }: {
    after: Date;
    type?: string;
    principal?: string;
  }) {
    return this.axios.get(uri`actuator/auditevents`, {
      params: {
        after: after.toISOString(),
        type: type,
        principal: principal,
      },
    });
  }

  async fetchSessionsByUsername(username?: string) {
    return this.axios.get(uri`actuator/sessions`, {
      params: {
        username: username,
      },
    });
  }

  async fetchSession(sessionId: string) {
    return this.axios.get(uri`actuator/sessions/${sessionId}`);
  }

  async deleteSession(sessionId: string) {
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

  streamLogfile(interval: number) {
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

  async readMBeanAttributes(domain: string, mBean: string) {
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

  async writeMBeanAttribute(
    domain: string,
    mBean: string,
    attribute: string,
    value: any,
  ) {
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

  async invokeMBeanOperation(
    domain: string,
    mBean: string,
    operation: string,
    args: any[],
  ) {
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

  async fetchSbom(id: string) {
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
  metadata?: { [key: string]: string };
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

type InstanceData = {
  id: string;
  registration: Registration;
  endpoints?: Endpoint[];
  availableMetrics?: string[];
  tags?: { [key: string]: string }[];
  statusTimestamp?: string;
  buildVersion?: string;
  statusInfo?: StatusInfo;
};

type Endpoint = {
  id: string;
  url: string;
};

export const DOWN_STATES = ['OUT_OF_SERVICE', 'DOWN', 'OFFLINE', 'RESTRICTED'];
export const UP_STATES = ['UP'];
export const UNKNOWN_STATES = ['UNKNOWN'];
