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
    this._listeners = {
      added: [],
      updated: [],
      removed: []
    };
  }

  indexOf(name) {
    return this.applications.findIndex(application => application.name === name)
  }

  addEventListener(type, listener) {
    if (!(type in this._listeners)) {
      this._listeners[type] = [];
    }
    this._listeners[type].push(listener);
  }

  removeEventListener(type, listener) {
    if (!(type in this._listeners)) {
      return;
    }

    const idx = this._listeners[type].indexOf(listener);
    if (idx > 0) {
      this._listeners[type].splice(idx, 1);
    }
  }

  dispatchEvent(type, ...args) {
    if (!(type in this._listeners)) {
      return;
    }
    const target = this;
    this._listeners[type].forEach(
      listener => listener.call(target, ...args)
    )
  }

  start() {
    const listing = Observable.defer(() => Application.list()).concatMap(message => message.data);
    const stream = Application.getStream().map(message => message.data);
    this.subscription = listing.concat(stream)
      .doFirst(() => this.dispatchEvent('connected'))
      .retryWhen(errors => errors
        .do(error => this.dispatchEvent('error', error))
        .delay(5000)
      ).subscribe({
        next: application => {
          const idx = this.indexOf(application.name);
          if (idx >= 0) {
            const oldApplication = this.applications[idx];
            if (application.instances.length > 0) {
              this.applications.splice(idx, 1, application);
              this.dispatchEvent('updated', application, oldApplication);
            } else {
              this.applications.splice(idx, 1);
              this.dispatchEvent('removed', oldApplication);
            }
          } else {
            this.applications.push(application);
            this.dispatchEvent('added', application);
          }
        }
      });
  }

  stop() {
    if (this.subscription) {
      try {
        !this.subscription.closed && this.subscription.unsubscribe();
      } finally {
        this.subscription = null;
      }
    }
  }
}