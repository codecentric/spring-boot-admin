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

import '@/assets/css/base.scss';
import moment from 'moment';
import Vue from 'vue';
import VueRouter from 'vue-router';
import components from './components';
import Notifications from './notifications';
import sbaShell from './shell';
import Store from './store';
import ViewRegistry from './viewRegistry';
import views from './views';

moment.locale(window.navigator.language);
Vue.use(VueRouter);
Vue.use(components);

const applicationStore = new Store();
const viewRegistry = new ViewRegistry();

const installables = [
  Notifications,
  ...views,
  /* global SBA */
  ...SBA.extensions
];

installables.forEach(view => view.install({
  viewRegistry,
  applicationStore,
  vue: Vue
}));

new Vue({
  router: new VueRouter({
    linkActiveClass: 'is-active',
    routes: viewRegistry.routes
  }),
  el: '#app',
  render(h) {
    return h(sbaShell, {
      props: {
        views: this.views,
        applications: this.applications,
        error: this.error
      }
    });
  },
  data: {
    views: viewRegistry.views,
    applications: applicationStore.applications,
    error: null
  },
  methods: {
    onError(error) {
      console.warn('Connection to server failed:', error);
      this.error = error;
    },
    onConnected() {
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
