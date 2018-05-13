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

import sbaInstancesAuditevents from './auditevents';
import sbaInstancesDetails from './details';
import sbaInstancesEnv from './env';
import sbaInstancesFlyway from './flyway';
import sbaInstancesTrace from './httptrace';
import sbaInstancesJolokia from './jolokia';
import sbaInstancesLiquibase from './liquibase';
import sbaInstancesLogfile from './logfile';
import sbaInstancesLoggers from './loggers';
import sbaInstancesMetrics from './metrics';
import sbaInstancesSessions from './sessions';
import sbaInstancesShell from './shell';
import sbaInstancesThreaddump from './threaddump';

export default [{
  path: '/instances/:instanceId', component: sbaInstancesShell, props: true,
  children: [{
    path: '', component: sbaInstancesDetails, props: true, name: 'instance/details'
  }, {
    path: 'metrics', component: sbaInstancesMetrics, props: true, name: 'instance/metrics'
  }, {
    path: 'env', component: sbaInstancesEnv, props: true, name: 'instance/env'
  }, {
    path: 'logfile', component: sbaInstancesLogfile, props: true, name: 'instance/logfile'
  }, {
    path: 'loggers', component: sbaInstancesLoggers, props: true, name: 'instance/loggers'
  }, {
    path: 'jolokia', component: sbaInstancesJolokia, props: true, name: 'instance/jolokia'
  }, {
    path: 'httptrace', component: sbaInstancesTrace, props: true, name: 'instance/httptrace'
  }, {
    path: 'auditevents', component: sbaInstancesAuditevents, props: true, name: 'instance/auditevents'
  }, {
    path: 'sessions', component: sbaInstancesSessions, props: true, name: 'instance/sessions'
  }, {
    path: 'liquibase', component: sbaInstancesLiquibase, props: true, name: 'instance/liquibase'
  }, {
    path: 'flyway', component: sbaInstancesFlyway, props: true, name: 'instance/flyway'
  }, {
    path: 'threaddump', component: sbaInstancesThreaddump, props: true, name: 'instance/threaddump'
  }]
}, {
  name: 'instance/details',
  handle: 'Details',
  order: 0
}, {
  name: 'instance/metrics',
  handle: 'Metrics',
  order: 50,
  isActive: ({instance}) => instance.hasEndpoint('metrics')
}, {
  name: 'instance/env',
  handle: 'Environment',
  order: 100,
  isActive: ({instance}) => instance.hasEndpoint('env')
}, {
  name: 'instance/logfile',
  handle: 'Logfile',
  order: 200,
  isActive: ({instance}) => instance.hasEndpoint('logfile')
}, {
  name: 'instance/loggers',
  handle: 'Loggers',
  order: 300,
  isActive: ({instance}) => instance.hasEndpoint('loggers')
}, {
  name: 'instance/jolokia',
  handle: 'JMX',
  order: 350,
  isActive: ({instance}) => instance.hasEndpoint('jolokia')
}, {
  name: 'instance/threaddump',
  handle: 'Threads',
  order: 400,
  isActive: ({instance}) => instance.hasEndpoint('threaddump')
}, {
  name: 'instance/httptrace',
  handle: 'Http Traces',
  order: 500,
  isActive: ({instance}) => instance.hasEndpoint('httptrace')
}, {
  name: 'instance/auditevents',
  handle: 'Audit Log',
  order: 600,
  isActive: ({instance}) => instance.hasEndpoint('auditevents')
}, {
  name: 'instance/sessions',
  handle: 'Sessions',
  order: 700,
  isActive: ({instance}) => instance.hasEndpoint('sessions')
}, {
  name: 'instance/heapdump',
  href: params => `instances/${params.instanceId}/actuator/heapdump`,
  handle: 'Heapdump',
  order: 800,
  isActive: ({instance}) => instance.hasEndpoint('heapdump')
}, {
  name: 'instance/liquibase',
  handle: 'Liquibase',
  order: 900,
  isActive: ({instance}) => instance.hasEndpoint('liquibase')
}, {
  name: 'instance/flyway',
  handle: 'Flyway',
  order: 900,
  isActive: ({instance}) => instance.hasEndpoint('flyway')
}];
