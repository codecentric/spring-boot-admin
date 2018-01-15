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

import moment from 'moment';
import Vue from 'vue';
import VueRouter from 'vue-router';
import './assets/css/base.scss';
import './assets/img/favicon.png';
import sbaComponents from './components'
import FontAwesomeIcon from './utils/fontawesome';
import sbaAbout from './views/about';
import sbaApplications from './views/applications';
import sbaInstancesAuditevents from './views/instances/auditevents';
import sbaInstancesDetails from './views/instances/details';
import sbaInstancesEnv from './views/instances/env';
import sbaInstancesFlyway from './views/instances/flyway';
import sbaInstancesLiquibase from './views/instances/liquibase';
import sbaInstancesLogfile from './views/instances/logfile';
import sbaInstancesLoggers from './views/instances/loggers';
import sbaInstancesSessions from './views/instances/sessions';
import sbaInstancesShell from './views/instances/shell';
import sbaInstancesThreaddump from './views/instances/threaddump';
import sbaInstancesTrace from './views/instances/trace';
import sbaJournal from './views/journal';
import sbaShell from './views/shell';

moment.locale(window.navigator.language);

Vue.use(VueRouter);
Vue.use(sbaComponents);
Vue.component('font-awesome-icon', FontAwesomeIcon);

const router = new VueRouter({
  linkActiveClass: 'is-active'
});

const views = [];
views.register = view => {
  if (view.template) {
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

views.register({
  path: '/applications',
  name: 'applications',
  template: 'Applications',
  order: 0,
  component: sbaApplications
});
views.register({path: '/journal', name: 'journal', template: 'Journal', order: 100, component: sbaJournal});
views.register({path: '/about', name: 'about', template: 'About', order: 200, component: sbaAbout});
views.register({
  path: '/instances/:instanceId', component: sbaInstancesShell, props: true,
  children: [{
    path: '', component: sbaInstancesDetails, props: true, name: 'instance/details',
  }, {
    path: 'env', component: sbaInstancesEnv, props: true, name: 'instance/env',
  }, {
    path: 'logfile', component: sbaInstancesLogfile, props: true, name: 'instance/logfile',
  }, {
    path: 'loggers', component: sbaInstancesLoggers, props: true, name: 'instance/loggers',
  }, {
    path: 'trace', component: sbaInstancesTrace, props: true, name: 'instance/trace',
  }, {
    path: 'auditevents', component: sbaInstancesAuditevents, props: true, name: 'instance/auditevents',
  }, {
    path: 'sessions/:sessionId?', component: sbaInstancesSessions, props: true, name: 'instance/sessions',
  }, {
    path: 'liquibase', component: sbaInstancesLiquibase, props: true, name: 'instance/liquibase',
  }, {
    path: 'flyway', component: sbaInstancesFlyway, props: true, name: 'instance/flyway',
  }, {
    path: 'threaddump', component: sbaInstancesThreaddump, props: true, name: 'instance/threaddump',
  }]
});
views.register({
  name: 'instance/details',
  template: 'Details',
  order: 0,
});
views.register({
  name: 'instance/env',
  template: 'Environment',
  order: 100,
  isActive: ({instance}) => instance.hasEndpoint('env')
});
views.register({
  name: 'instance/logfile',
  template: 'Logfile',
  order: 200,
  isActive: ({instance}) => instance.hasEndpoint('logfile')
});
views.register({
  name: 'instance/loggers',
  template: 'Loggers',
  order: 300,
  isActive: ({instance}) => instance.hasEndpoint('loggers')
});
views.register({
  name: 'instance/threaddump',
  template: 'Threads',
  order: 400,
  isActive: ({instance}) => instance.hasEndpoint('threaddump')
});
views.register({
  name: 'instance/trace',
  template: 'Traces',
  order: 500,
  isActive: ({instance}) => instance.hasEndpoint('traces')
});
views.register({
  name: 'instance/auditevents',
  template: 'Audit Log',
  order: 600,
  isActive: ({instance}) => instance.hasEndpoint('auditevents')
});
views.register({
  name: 'instance/sessions',
  template: 'Sessions',
  order: 700,
  isActive: ({instance}) => instance.hasEndpoint('sessions')
});
views.register({
  name: 'instance/heapdump',
  href: params => `instances/${params.instanceId}/actuator/heapdump`,
  template: 'Heapdump',
  order: 800,
  isActive: ({instance}) => instance.hasEndpoint('heapdump')
});
views.register({
  name: 'instance/liquibase',
  template: 'Liquibase',
  order: 900,
  isActive: ({instance}) => instance.hasEndpoint('liquibase')
});
views.register({
  name: 'instance/flyway',
  template: 'Flyway',
  order: 900,
  isActive: ({instance}) => instance.hasEndpoint('flyway')
});

router.addRoutes([{path: '/', redirect: {name: 'applications'}}]);

//Fire root Vue up
new Vue({
  router,
  el: '#app',
  render(h) {
    return h(sbaShell);
  },
  data: {
    views
  }
});
