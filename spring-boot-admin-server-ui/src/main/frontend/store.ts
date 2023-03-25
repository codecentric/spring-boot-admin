/*
 * Copyright 2014-2019 the original author or authors.
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
import Application from './services/application.js';
import {
  bufferTime,
  concat,
  concatMap,
  defer,
  delay,
  filter,
  map,
  retryWhen,
  tap,
} from './utils/rxjs.js';

export const findInstance = (applications, instanceId) => {
  for (let application of applications) {
    const instance = application.findInstance(instanceId);
    if (instance) {
      return instance;
    }
  }
  return undefined;
};

export const findApplicationForInstance = (applications, instanceId) => {
  return applications.find((application) =>
    Boolean(application.findInstance(instanceId))
  );
};

export default class ApplicationStore {
  constructor() {
    this._listeners = {};
    this._applications = new Map();
    this.applications = [];
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

  _dispatchEvent(type, ...args) {
    if (!(type in this._listeners)) {
      return;
    }
    const target = this;
    this._listeners[type].forEach((listener) => listener.call(target, ...args));
  }

  start() {
    // Do not resubscribe when already started
    if (this.subscription !== undefined) {
      return;
    }
    const list = defer(() => Application.list()).pipe(
      tap(() => this._dispatchEvent('connected')),
      concatMap((message) => message.data)
    );

    const stream = Application.getStream().pipe(map((message) => message.data));

    this.subscription = concat(list, stream)
      .pipe(
        retryWhen((errors) =>
          errors.pipe(
            tap((error) => this._dispatchEvent('error', error)),
            delay(5000)
          )
        ),
        bufferTime(250),
        filter((a) => a.length > 0)
      )
      .subscribe({
        next: (applications) => this.updateApplications(applications),
      });
  }

  updateApplications(applications) {
    applications.forEach((a) => this.updateApplication(a));
    this.applications = [...this._applications.values()];
    this._dispatchEvent('changed', this.applications);
  }

  updateApplication(application) {
    const oldApplication = this._applications.get(application.name);
    if (!oldApplication && application.instances.length > 0) {
      this._applications.set(application.name, application);
      this._dispatchEvent('added', application);
    } else if (oldApplication && application.instances.length > 0) {
      this._applications.set(application.name, application);
      this._dispatchEvent('updated', application, oldApplication);
    } else if (oldApplication && application.instances.length <= 0) {
      this._applications.delete(application.name);
      this._dispatchEvent('removed', oldApplication);
    }
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
