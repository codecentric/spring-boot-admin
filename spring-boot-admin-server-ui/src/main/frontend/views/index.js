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

import {view as aboutView} from './about';
import {view as applicationView} from './applications';
import sbaInstancesAuditevents from './instances/auditevents';
import sbaInstancesDetails from './instances/details';
import sbaInstancesEnv from './instances/env';
import sbaInstancesFlyway from './instances/flyway';
import sbaInstancesTrace from './instances/httptrace';
import sbaInstancesJolokia from './instances/jolokia';
import sbaInstancesLiquibase from './instances/liquibase';
import sbaInstancesLogfile from './instances/logfile';
import sbaInstancesLoggers from './instances/loggers';
import sbaInstancesSessions from './instances/sessions';
import sbaInstancesShell from './instances/shell';
import sbaInstancesThreaddump from './instances/threaddump';
import {view as journalView} from './journal';

export default router => {
  const views = [];
  views.register = view => {
    if (view.handle) {
      if (typeof view.handle === 'string') {
        const label = view.handle;
        view.handle = {
          render() {
            return this._v(label)
          }
        }
      }
      views.push(view);
    }

    if (view.component) {
      router.addRoutes([{
          path: view.path,
          children: view.children,
          component: view.component,
          props: view.props,
          name: view.name
        }]
      )
    }
  };

  views.register(applicationView);
  views.register(journalView);
  views.register(aboutView);
  views.register({
    path: '/instances/:instanceId', component: sbaInstancesShell, props: true,
    children: [{
      path: '', component: sbaInstancesDetails, props: true, name: 'instance/details'
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
  });
  views.register({
    name: 'instance/details',
    handle: 'Details',
    order: 0
  });
  views.register({
    name: 'instance/env',
    handle: 'Environment',
    order: 100,
    isActive: ({instance}) => instance.hasEndpoint('env')
  });
  views.register({
    name: 'instance/logfile',
    handle: 'Logfile',
    order: 200,
    isActive: ({instance}) => instance.hasEndpoint('logfile')
  });
  views.register({
    name: 'instance/loggers',
    handle: 'Loggers',
    order: 300,
    isActive: ({instance}) => instance.hasEndpoint('loggers')
  });
  views.register({
    name: 'instance/jolokia',
    handle: 'JMX',
    order: 350,
    isActive: ({instance}) => instance.hasEndpoint('jolokia')
  });
  views.register({
    name: 'instance/threaddump',
    handle: 'Threads',
    order: 400,
    isActive: ({instance}) => instance.hasEndpoint('threaddump')
  });
  views.register({
    name: 'instance/httptrace',
    handle: 'Http Traces',
    order: 500,
    isActive: ({instance}) => instance.hasEndpoint('httptrace')
  });
  views.register({
    name: 'instance/auditevents',
    handle: 'Audit Log',
    order: 600,
    isActive: ({instance}) => instance.hasEndpoint('auditevents')
  });
  views.register({
    name: 'instance/sessions',
    handle: 'Sessions',
    order: 700,
    isActive: ({instance}) => instance.hasEndpoint('sessions')
  });
  views.register({
    name: 'instance/heapdump',
    href: params => `instances/${params.instanceId}/actuator/heapdump`,
    handle: 'Heapdump',
    order: 800,
    isActive: ({instance}) => instance.hasEndpoint('heapdump')
  });
  views.register({
    name: 'instance/liquibase',
    handle: 'Liquibase',
    order: 900,
    isActive: ({instance}) => instance.hasEndpoint('liquibase')
  });
  views.register({
    name: 'instance/flyway',
    handle: 'Flyway',
    order: 900,
    isActive: ({instance}) => instance.hasEndpoint('flyway')
  });

  router.addRoutes([{path: '/', redirect: {name: 'applications'}}]);

  return views;
}