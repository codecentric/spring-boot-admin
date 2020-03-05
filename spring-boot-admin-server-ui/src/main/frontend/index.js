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

import '@/assets/css/base.scss';
import axios from '@/utils/axios';
import moment from 'moment';
import 'moment/locale/de';
import Vue from 'vue';
import VueRouter from 'vue-router';
import components from './components';
import i18n from './i18n';
import Notifications from './notifications';
import sbaConfig from './sba-config'
import sbaShell from './shell';
import Store from './store';
import ViewRegistry from './viewRegistry';
import views from './views';

moment.locale(navigator.language.split('-')[0]);
Vue.use(VueRouter);
Vue.use(components);

const applicationStore = new Store();
const viewRegistry = new ViewRegistry();

const installables = [
  Notifications,
  ...views,
  ...sbaConfig.extensions
];

installables.forEach(view => view.install({
  viewRegistry,
  applicationStore,
  vue: Vue,
  vueI18n: i18n,
  axios
}));

const routesKnownToBackend = sbaConfig.uiSettings.routes.map(r => new RegExp(`^${r.replace('/**', '(/.*)?')}$`));
const unknownRoutes = viewRegistry.routes.filter(vr => vr.path !== '/' && !routesKnownToBackend.some(br => br.test(vr.path)));
if (unknownRoutes.length > 0) {
  console.warn(`The routes ${JSON.stringify(unknownRoutes.map(r => r.path))} aren't known to the backend and may be not properly routed!`)
}

new Vue({
  i18n,
  router: new VueRouter({
    mode: 'history',
    linkActiveClass: 'is-active',
    routes: viewRegistry.routes
  }),
  el: '#app',
  render(h) {
    return h(sbaShell, {
      props: {
        views: this.views,
        applications: this.applications,
        applicationsInitialized: this.applicationsInitialized,
        error: this.error
      }
    });
  },
  data: {
    views: viewRegistry.views,
    applications: applicationStore.applications,
    applicationsInitialized: false,
    error: null
  },
  methods: {
    onError(error) {
      console.warn('Connection to server failed:', error);
      this.applicationsInitialized = true;
      this.error = error;
    },
    onConnected() {
      this.applicationsInitialized = true;
      this.error = null;
    }
  },
  created() {
    applicationStore.addEventListener('connected', this.onConnected);
    applicationStore.addEventListener('error', this.onError);
    applicationStore.start();
  },
  beforeDestroy() {
    applicationStore.stop();
    applicationStore.removeEventListener('connected', this.onConnected);
    applicationStore.removeEventListener('error', this.onError)
  }
});
