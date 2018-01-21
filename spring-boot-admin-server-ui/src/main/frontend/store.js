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
import Application from '@/services/application';
import {Observable} from '@/utils/rxjs';

export default class {
  constructor() {
    this.applications = [];
    this.listeners = {
      added: [],
      updated: [],
      removed: []
    };
  }

  indexOf(name) {
    return this.applications.findIndex(application => application.name === name)
  }

  addEventListener(type, listener) {
    if (!(type in this.listeners)) {
      this.listeners[type] = [];
    }
    this.listeners[type].push(listener);
  }

  removeEventListener(type, listener) {
    if (!(type in this.listeners)) {
      return;
    }

    const idx = this.listeners[type].indexOf(listener);
    if (idx > 0) {
      this.listeners[type].splice(idx, 1);
    }
  }

  dispatchEvent(type, ...args) {
    if (!(type in this.listeners)) {
      return;
    }
    const target = this;
    this.listeners[type].forEach(
      listener => listener.call(target, ...args)
    )
  }

  start() {
    const initialListing = Observable.from(Application.list())
      .concatMap(message => message.data);
    const stream = Application.getStream()
      .map(message => message.data);
    this.subscription = initialListing.concat(stream)
      .subscribe({
        next: application => {
          const idx = this.indexOf(application.name);
          if (idx >= 0) {
            const oldVal = this.applications[idx];
            if (application.instances.length > 0) {
              this.applications.splice(idx, 1, application);
              this.dispatchEvent('updated', application, oldVal);
            } else {
              this.applications.splice(idx, 1);
              this.dispatchEvent('removed', oldVal);
            }
          } else {
            this.applications.push(application);
            this.dispatchEvent('added', application);
          }
        },
        error: err => {
        }
      });
  }

  stop() {
    if (this.subscription) {
      try {
        this.subscription.unsubscribe();
      } finally {
        this.subscription = null;
      }
    }
  }
}