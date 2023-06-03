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
import { createApp } from 'vue';

import './login.css';

import i18n from './i18n';
import Login from './login/login.vue';

const app = createApp(Login, {
  csrf: window.csrf,
  icon: window.uiSettings.loginIcon,
  title: window.uiSettings.title,
  theme: window.uiSettings.theme,
  param: window.param,
});
app.use(i18n);
app.mount('#login');
