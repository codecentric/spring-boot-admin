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

import axios from 'axios';
import moment from 'moment';
import Vue from 'vue';
import VueRouter from 'vue-router';
import './assets/css/base.scss';
import logoDanger from './assets/img/favicon-danger.png';
import logoOk from './assets/img/favicon.png';
import sbaComponents from './components'
import Store from './store';
import FontAwesomeIcon from './utils/fontawesome';
import Notifications from './utils/notifications';
import createViews from './views';
import sbaShell from './views/shell';

moment.locale(window.navigator.language);

Vue.use(VueRouter);
Vue.use(sbaComponents);
Vue.component('font-awesome-icon', FontAwesomeIcon);

const router = new VueRouter({
  linkActiveClass: 'is-active'
});

Notifications.requestPermissions();
axios.interceptors.request.use(config => {
  config.headers['X-Requested-With'] = 'XMLHttpRequest';
  return config;
});

axios.interceptors.response.use(response => response, error => {
  if (401 === error.response.status) {
    window.location = `login?redirectTo=${encodeURIComponent(window.location.href)}`;
  }
  return Promise.reject(error);
});

const store = new Store();

store.addEventListener('updated', (newVal, oldVal) => {
  if (newVal.status !== oldVal.status) {
    Notifications.notify(`${newVal.name} is now ${newVal.status}`, {
      tag: `${newVal.name}-${newVal.status}`,
      lang: 'en',
      body: `was ${oldVal.status}.`,
      icon: newVal.status === 'UP' ? logoOk : logoDanger,
      renotify: true,
      timeout:
        10000
    })
  }
});

new Vue({
  router,
  el: '#app',
  render(h) {
    return h(sbaShell);
  },
  data: {
    views: createViews(router),
    applications: store.applications
  },
  computed: {
    allUp() {
      return this.applications.findIndex(application => application.status !== 'UP') < 0;
    }
  },
  watch: {
    allUp(newVal) {
      const link = document.querySelector('link[rel*="icon"]');
      link.href = newVal ? logoOk : logoDanger;
    }
  },
  created() {
    store.start();
  },
  beforeDestroy() {
    store.stop();
  },
});
