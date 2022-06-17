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

export const applications = [
  {
    name: 'spring-boot-admin-sample-servlet',
    buildVersion: '2.5.2-SNAPSHOT',
    status: 'UP',
    statusTimestamp: '2021-10-29T06:50:09.600276Z',
    instances: [
      {
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
          {
            id: 'threaddump',
            url: 'http://localhost:8080/actuator/threaddump',
          },
          {
            id: 'metrics',
            url: 'http://localhost:8080/actuator/metrics',
          },
          {
            id: 'conditions',
            url: 'http://localhost:8080/actuator/conditions',
          },
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
      },
    ],
  },

  {
    name: 'a-bootiful-client',
    buildVersion: null,
    status: 'UP',
    statusTimestamp: '2022-06-17T16:54:05.515548Z',
    instances: [
      {
        id: 'c8517142a475',
        version: 2,
        registration: {
          name: 'a-bootiful-client',
          managementUrl: 'http://macbookm1-max.fritz.box:9192/actuator',
          healthUrl: 'http://macbookm1-max.fritz.box:9192/actuator/health',
          serviceUrl: 'http://macbookm1-max.fritz.box:9191/',
          source: 'http-api',
          metadata: {
            startup: '2022-06-17T18:52:48.49487+02:00',
          },
        },
        registered: true,
        statusInfo: {
          status: 'UP',
          details: {},
        },
        statusTimestamp: '2022-06-17T16:52:48.691181Z',
        info: {},
        endpoints: [
          {
            id: 'caches',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/caches',
          },
          {
            id: 'loggers',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/loggers',
          },
          {
            id: 'health',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/health',
          },
          {
            id: 'refresh',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/refresh',
          },
          {
            id: 'env',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/env',
          },
          {
            id: 'heapdump',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/heapdump',
          },
          {
            id: 'features',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/features',
          },
          {
            id: 'scheduledtasks',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/scheduledtasks',
          },
          {
            id: 'mappings',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/mappings',
          },
          {
            id: 'beans',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/beans',
          },
          {
            id: 'configprops',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/configprops',
          },
          {
            id: 'threaddump',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/threaddump',
          },
          {
            id: 'metrics',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/metrics',
          },
          {
            id: 'conditions',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/conditions',
          },
          {
            id: 'info',
            url: 'http://macbookm1-max.fritz.box:9192/actuator/info',
          },
        ],
        buildVersion: null,
        tags: {},
      },
      {
        id: '3c5eda806436',
        version: 2,
        registration: {
          name: 'a-bootiful-client',
          managementUrl: 'http://macbookm1-max.fritz.box:9091/actuator',
          healthUrl: 'http://macbookm1-max.fritz.box:9091/actuator/health',
          serviceUrl: 'http://macbookm1-max.fritz.box:9090/',
          source: 'http-api',
          metadata: {
            startup: '2022-06-17T18:54:05.319264+02:00',
          },
        },
        registered: true,
        statusInfo: {
          status: 'UP',
          details: {},
        },
        statusTimestamp: '2022-06-17T16:54:05.515548Z',
        info: {},
        endpoints: [
          {
            id: 'caches',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/caches',
          },
          {
            id: 'loggers',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/loggers',
          },
          {
            id: 'health',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/health',
          },
          {
            id: 'refresh',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/refresh',
          },
          {
            id: 'env',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/env',
          },
          {
            id: 'heapdump',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/heapdump',
          },
          {
            id: 'features',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/features',
          },
          {
            id: 'scheduledtasks',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/scheduledtasks',
          },
          {
            id: 'mappings',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/mappings',
          },
          {
            id: 'beans',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/beans',
          },
          {
            id: 'configprops',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/configprops',
          },
          {
            id: 'threaddump',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/threaddump',
          },
          {
            id: 'metrics',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/metrics',
          },
          {
            id: 'conditions',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/conditions',
          },
          {
            id: 'info',
            url: 'http://macbookm1-max.fritz.box:9091/actuator/info',
          },
        ],
        buildVersion: null,
        tags: {},
      },
    ],
  },
];
