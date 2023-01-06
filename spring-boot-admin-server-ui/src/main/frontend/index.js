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
import NotificationcenterPlugin from '@stekoe/vue-toast-notificationcenter';
import moment from 'moment';
import { createApp, h, onBeforeMount, onBeforeUnmount, reactive } from 'vue';
import { useI18n } from 'vue-i18n';



import './index.css';



import components from './components';
import { createViewRegistry } from './composables/ViewRegistry.js';
import { createApplicationStore, useApplicationStore } from './composables/useApplicationStore.js';
import i18n from './i18n';
import { worker } from './mocks/browser';
import Notifications from './notifications.js';
import SbaModalPlugin from './plugins/modal';
import router from './router.js';
import sbaConfig from './sba-config.js';
import sbaShell from './shell/index.vue';
import axios from './utils/axios.js';
import views from './views';


moment.locale(navigator.language.split('-')[0]);

const applicationStore = createApplicationStore();
const viewRegistry = createViewRegistry();

const installables = [Notifications, ...views, ...sbaConfig.extensions];

if (process.env.NODE_ENV === 'development') {
  worker.start({
    serviceWorker: {
      url: './mockServiceWorker.js',
    },
  });
}

installables.forEach((installable) => {
  installable.install({
    viewRegistry,
    applicationStore,
  });
});

const app = createApp({
  setup() {
    const { applications, applicationsInitialized, error } =
      useApplicationStore();
    const { t } = useI18n();
    let props = reactive({
      applications,
      applicationsInitialized,
      error,
      t,
    });

    onBeforeMount(() => {
      applicationStore.start();
    });

    onBeforeUnmount(() => {
      applicationStore.stop();
    });

    return () => h(sbaShell, props);
  },
});

app.use(i18n);
app.use(components);
app.use(NotificationcenterPlugin, {
  duration: 10_000,
});
app.use(SbaModalPlugin, { i18n });
app.use(router(viewRegistry.routes));

const vue = app.mount('#app');

installables.forEach((view) => {
  try {
    view.configure
      ? view.configure({
          vue,
          i18n: vue.$i18n,
          axios,
        })
      : void 0;
  } catch (e) {
    console.error(`Error configuring view ${view}`, e);
  }
});
