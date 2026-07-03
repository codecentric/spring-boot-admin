/*
 * Copyright 2014-2026 the original author or authors.
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
import { AxiosError } from 'axios';

import axios, {
  addLanguageHeaderInterceptor,
  redirectOn401,
  registerErrorToastInterceptor,
} from '../utils/axios';
import uri from '../utils/uri';

import { actuatorMimeTypes } from '@/services/spring-mime-types';

export type FetchMetricOptions = {
  suppressToast?: boolean | ((error: AxiosError) => boolean);
};

const isInstanceActuatorRequest = (url: string) =>
  url.match(/^instances[/][^/]+[/]actuator([/].*)?$/);

/**
 * Returns a stable set of actuator HTTP functions for a given instanceId.
 * The axios client is created once per call — callers are responsible for
 * caching the result (e.g. via a module-level Map or a composable cache).
 */
function createInstanceClient(instanceId: string) {
  const client = axios.create({
    withCredentials: true,
    baseURL: uri`instances/${instanceId}`,
    headers: { Accept: actuatorMimeTypes.join(',') },
  });
  client.interceptors.request.use(addLanguageHeaderInterceptor);
  client.interceptors.response.use(
    (response) => response,
    redirectOn401(
      (error) =>
        !isInstanceActuatorRequest(
          (error.config?.baseURL ?? '') + (error.config?.url ?? ''),
        ),
    ),
  );
  registerErrorToastInterceptor(client);
  return client;
}

// Module-level cache: one axios client per instanceId, never recreated on SSE.
const clientCache = new Map<string, ReturnType<typeof createInstanceClient>>();

function getClient(instanceId: string) {
  if (!clientCache.has(instanceId)) {
    clientCache.set(instanceId, createInstanceClient(instanceId));
  }
  return clientCache.get(instanceId)!;
}

export function useInstanceService(instanceId: string) {
  const client = getClient(instanceId);

  async function fetchMetrics() {
    return client.get(uri`actuator/metrics`);
  }

  async function fetchMetric(
    metric: string,
    tags: Record<string, any> = {},
    options?: FetchMetricOptions,
  ) {
    const params = new URLSearchParams();
    let firstElementDuplicated = false;
    Object.entries(tags)
      .filter(([, value]) => typeof value !== 'undefined' && value !== null)
      .forEach(([name, value]) => {
        params.append('tag', `${name}:${value}`);
        if (!firstElementDuplicated) {
          // workaround: https://github.com/spring-projects/spring-framework/issues/23820
          params.append('tag', `${name}:${value}`);
          firstElementDuplicated = true;
        }
      });

    return client.get(uri`actuator/metrics/${metric}`, {
      params,
      suppressToast: options?.suppressToast,
    });
  }

  async function fetchHealth() {
    return client.get(uri`actuator/health`, { validateStatus: null });
  }

  async function fetchHealthGroup(groupName: string) {
    return client.get(uri`actuator/health/${groupName}`, {
      validateStatus: null,
    });
  }

  async function fetchInfo() {
    return client.get(uri`actuator/info`);
  }

  async function fetchEnv(name?: string) {
    return client.get(uri`actuator/env/${name || ''}`);
  }

  return {
    fetchMetrics,
    fetchMetric,
    fetchHealth,
    fetchHealthGroup,
    fetchInfo,
    fetchEnv,
  };
}
