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
import * as Vue from 'vue';
import { createApp, h, onBeforeMount, onBeforeUnmount, reactive } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';

import components from './components';
import {
  CUSTOM_ROUTES_ADDED_EVENT,
  createViewRegistry,
  useViewRegistry,
} from './composables/ViewRegistry.js';
import {
  createApplicationStore,
  useApplicationStore,
} from './composables/useApplicationStore.js';
import i18n from './i18n';
import { worker } from './mocks/browser';
import Notifications from './notifications.js';
import SbaModalPlugin from './plugins/modal';
import sbaConfig from './sba-config';
import views from './views';

import eventBus from '@/services/bus';
import sbaShell from '@/shell';

const applicationStore = createApplicationStore();
const viewRegistry = createViewRegistry();

if (process.env.NODE_ENV === 'development') {
  globalThis.__VUE_OPTIONS_API__ = true;
  globalThis.__VUE_PROD_DEVTOOLS__ = true;
}

globalThis.Vue = Vue;
globalThis.SBA.viewRegistry = useViewRegistry();
globalThis.SBA.useApplicationStore = useApplicationStore;
globalThis.SBA.useI18n = () => i18n.global;
globalThis.SBA.use = ({ install }) => {
  install({
    viewRegistry: globalThis.SBA.viewRegistry,
    applicationStore: globalThis.SBA.useApplicationStore,
    i18n: i18n.global,
  });
};

sbaConfig.extensions.js.forEach((extension) => {
  const script = document.createElement('script');
  script.src = `extensions/${extension.resourcePath}`;
  document.head.appendChild(script);
});

sbaConfig.extensions.css.forEach((extension) => {
  const link = document.createElement('link');
  link.rel = 'stylesheet';
  link.href = `extensions/${extension.resourcePath}`;
  document.head.appendChild(link);
});

moment.locale(navigator.language.split('-')[0]);

if (process.env.NODE_ENV === 'development') {
  await worker.start();
}

const installables = [Notifications, ...views];
installables.forEach((installable) => {
  installable.install({
    viewRegistry,
    applicationStore,
  });
});

const app = createApp({
  setup() {
    const router = useRouter();
    const route = useRoute();
    const { applications, applicationsInitialized, error } =
      useApplicationStore();
    const { t } = useI18n();

    onBeforeMount(() => {
      applicationStore.start();
    });

    onBeforeUnmount(() => {
      applicationStore.stop();
    });

    const routesAddedEventHandler = async () => {
      eventBus.off(CUSTOM_ROUTES_ADDED_EVENT, routesAddedEventHandler);
      await router.replace(route);
    };
    eventBus.on(CUSTOM_ROUTES_ADDED_EVENT, routesAddedEventHandler);

    return () =>
      h(
        sbaShell,
        reactive({
          applications,
          applicationsInitialized,
          error,
          t,
        }),
      );
  },
});

app.use(i18n);
app.use(components);
app.use(NotificationcenterPlugin, {
  duration: 10_000,
});
app.use(SbaModalPlugin, { i18n });
app.use(viewRegistry.createRouter());
app.mount('#app');
