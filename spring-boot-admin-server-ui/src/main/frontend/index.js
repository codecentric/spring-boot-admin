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

import './index.css';
import axios from './utils/axios.js';
import moment from 'moment';
import components from './components';
import Notifications from './notifications.js';
import sbaConfig from './sba-config.js'
import sbaShell from './shell/index.vue';
import views from './views';
import {createApp, h, reactive} from 'vue';
import i18n from './i18n';
import router from './router.js';
import {createApplicationStore, useApplicationStore} from "./composables/useApplicationStore.js";
import {createViewRegistry, useViewRegistry} from "./composables/ViewRegistry.js";
import about from "./views/about/index.vue";

moment.locale(navigator.language.split('-')[0]);

const applicationStore = createApplicationStore();
const viewRegistry = createViewRegistry();

const installables = [
  Notifications,
  ...views,
  ...sbaConfig.extensions
];

installables.forEach(installable => {
  installable.install({
    viewRegistry,
    applicationStore,
  })
});


const app = createApp({
  setup() {
    const {applications, applicationsInitialized, error} = useApplicationStore();

    let props = reactive({
      applications,
      applicationsInitialized,
      error
    });

    return () => h(sbaShell, props);
  }
})

app.use(components);
app.use(i18n);
app.use(router(viewRegistry.routes));

const vue = app.mount('#app');

installables.forEach(view => view.configure ? view.configure({
  vue,
  i18n: vue.$i18n,
  axios
}) : void (0));
