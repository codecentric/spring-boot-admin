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
let granted = false;

const requestPermissions = () => {
  if ('Notification' in window) {
    granted = (window.Notification.permission === 'granted');
    if (!granted && window.Notification.permission !== 'denied') {
      window.Notification.requestPermission(permission => granted = (permission === 'granted'));
    }
  }
};

const notify = (title, options) => {
  if (granted) {
    const note = new window.Notification(title, options);
    if (options.url !== null) {
      note.onclick = () => {
        window.focus();
        window.open(options.url, '_self');
      };
    }
    if (options.timeout > 0) {
      note.onshow = () => setTimeout(() => note.close(), options.timeout);
    }
  }
};

export default {
  install: ({applicationStore}) => {
    requestPermissions();

    applicationStore.addEventListener('updated', (newVal, oldVal) => {
      if (newVal.status !== oldVal.status) {
        notify(`${newVal.name} is now ${newVal.status}`, {
          tag: `${newVal.name}-${newVal.status}`,
          lang: 'en',
          body: `was ${oldVal.status}.`,
          icon: newVal.status === 'UP' ? 'assets/img/favicon.png' : 'assets/img/favicon-danger.png',
          renotify: true,
          timeout: 10000
        })
      }
    });
  }
}
