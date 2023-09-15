/*
 * Copyright 2014-2021 the original author or authors.
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
import { HealthStatus } from '../../HealthStatus';

export const instance = {
  id: 'bba333956ae6',
  version: 3,
  registration: {
    name: 'spring-boot-admin-sample-servlet',
    managementUrl: 'http://localhost:8080/actuator',
    healthUrl: 'http://localhost:8080/actuator/health',
    serviceUrl: 'http://localhost:8080/',
    source: 'http-api',
    metadata: {
      startup: '2021-10-29T08:50:07.486289+02:00',
      'tags.environment': 'test',
    },
  },
  registered: true,
  statusInfo: {
    status: 'UP',
    details: {
      db: {
        status: 'UP',
        details: {
          database: 'HSQL Database Engine',
          validationQuery: 'isValid()',
        },
      },
      diskSpace: {
        status: 'UP',
        details: {
          total: 499963174912,
          free: 108980899840,
          threshold: 10485760,
          exists: true,
        },
      },
      ping: { status: 'UP' },
    },
  },
  statusTimestamp: '2021-10-29T06:50:09.600276Z',
  info: {
    tags: { security: 'insecure' },
    'scm-url': '@scm.url@',
    'build-url': 'https://travis-ci.org/codecentric/spring-boot-admin',
    build: {
      artifact: 'spring-boot-admin-sample-servlet',
      name: 'Spring Boot Admin Sample Servlet',
      time: '2021-09-17T09:53:18.987Z',
      version: '2.5.2-SNAPSHOT',
      group: 'de.codecentric',
    },
  },
  endpoints: [
    { id: 'sessions', url: 'http://localhost:8080/actuator/sessions' },
    {
      id: 'httptrace',
      url: 'http://localhost:8080/actuator/httptrace',
    },
    {
      id: 'httptexchanges',
      url: 'http://localhost:8080/actuator/httpexchanges',
    },
    { id: 'caches', url: 'http://localhost:8080/actuator/caches' },
    {
      id: 'loggers',
      url: 'http://localhost:8080/actuator/loggers',
    },
    { id: 'logfile', url: 'http://localhost:8080/actuator/logfile' },
    {
      id: 'custom',
      url: 'http://localhost:8080/actuator/custom',
    },
    { id: 'health', url: 'http://localhost:8080/actuator/health' },
    {
      id: 'env',
      url: 'http://localhost:8080/actuator/env',
    },
    { id: 'heapdump', url: 'http://localhost:8080/actuator/heapdump' },
    {
      id: 'scheduledtasks',
      url: 'http://localhost:8080/actuator/scheduledtasks',
    },
    { id: 'mappings', url: 'http://localhost:8080/actuator/mappings' },
    {
      id: 'startup',
      url: 'http://localhost:8080/actuator/startup',
    },
    { id: 'beans', url: 'http://localhost:8080/actuator/beans' },
    {
      id: 'configprops',
      url: 'http://localhost:8080/actuator/configprops',
    },
    { id: 'threaddump', url: 'http://localhost:8080/actuator/threaddump' },
    {
      id: 'metrics',
      url: 'http://localhost:8080/actuator/metrics',
    },
    { id: 'conditions', url: 'http://localhost:8080/actuator/conditions' },
    {
      id: 'auditevents',
      url: 'http://localhost:8080/actuator/auditevents',
    },
    { id: 'info', url: 'http://localhost:8080/actuator/info' },
    {
      id: 'shutdown',
      url: 'http://localhost:8080/actuator/shutdown',
    },
    {
      id: 'restart',
      url: 'http://localhost:8080/actuator/restart',
    },
  ],
  buildVersion: '2.5.2-SNAPSHOT',
  tags: { environment: 'test', security: 'insecure' },
};

export const applications = Object.entries(HealthStatus).map((e) => {
  const STATUS = e[0];

  return {
    name: `application-${STATUS}`,
    buildVersion: '2.5.2-SNAPSHOT',
    status: STATUS,
    statusTimestamp: '2021-10-29T06:50:09.600276Z',
    instances: [instance],
  };
});
