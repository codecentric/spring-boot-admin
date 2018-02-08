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
import logoDanger from '@/assets/img/favicon-danger.png';
import logoOk from '@/assets/img/favicon.png';
import moment from 'moment';
import Vue from 'vue';
import VueRouter from 'vue-router';
import sbaComponents from './components';
import Store from './store';
import FontAwesomeIcon from './utils/fontawesome';
import Notifications from './utils/notifications';
import createViews from './views';
import sbaShell from './views/shell';

moment.locale(window.navigator.language);

Notifications.requestPermissions();

const store = new Store();

store.addEventListener('updated', (newVal, oldVal) => {
  if (newVal.status !== oldVal.status) {
    Notifications.notify(`${newVal.name} is now ${newVal.status}`, {
      tag: `${newVal.name}-${newVal.status}`,
      lang: 'en',
      body: `was ${oldVal.status}.`,
      icon: newVal.status === 'UP' ? logoOk : logoDanger,
      renotify: true,
      timeout: 10000
    })
  }
});

Vue.component('font-awesome-icon', FontAwesomeIcon);
Vue.use(VueRouter);
Vue.use(sbaComponents);

const router = new VueRouter({
  linkActiveClass: 'is-active'
});

new Vue({
  router,
  el: '#app',
  render(h) {
    return h(sbaShell);
  },
  data: {
    views: createViews(router),
    applications: store.applications,
    connectionFailed: false
  },
  methods: {
    onError(error) {
      console.warn('Connection to server failed:', error);
      this.connectionFailed = true;
    },
    onConnected() {
      this.connectionFailed = false;
    }
  },
  created() {
    store.addEventListener('connected', this.onConnected);
    store.addEventListener('error', this.onError);
    store.start();
  },
  beforeDestroy() {
    store.stop();
    store.removeEventListener('connected', this.onConnected);
    store.removeEventListener('error', this.onError)
  },
});
