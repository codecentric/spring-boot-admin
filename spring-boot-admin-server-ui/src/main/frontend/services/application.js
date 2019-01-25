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

import axios, {redirectOn401} from '@/utils/axios';
import waitForPolyfill from '@/utils/eventsource-polyfill';
import {concat, from, ignoreElements, Observable} from '@/utils/rxjs';
import uri from '@/utils/uri';
import sortBy from 'lodash/sortBy';
import Instance from './instance';

class Application {
  constructor({name, instances, ...application}) {
    Object.assign(this, application);
    this.name = name;
    this.axios = axios.create({
      baseURL: uri`applications/${this.name}/`
    });
    this.axios.interceptors.response.use(
      response => response,
      redirectOn401()
    );
    this.instances = sortBy(instances.map(i => new Instance(i), [instance => instance.registration.healthUrl]));
  }

  filterInstances(predicate) {
    return new Application({
      ...this,
      instances: this.instances.filter(predicate)
    })
  }

  findInstance(instanceId) {
    return this.instances.find(instance => instance.id === instanceId);
  }

  get isUnregisterable() {
    return this.instances.findIndex(i => i.isUnregisterable) >= 0;
  }

  async unregister() {
    return this.axios.delete('')
  }

  static async list() {
    return await axios.get('applications', {
      transformResponse: Application._transformResponse
    });
  }

  static getStream() {
    return concat(
      from(waitForPolyfill()).pipe(ignoreElements()),
      Observable.create(observer => {
        const eventSource = new EventSource('applications');
        eventSource.onmessage = message => observer.next({
          ...message,
          data: Application._transformResponse(message.data)
        });

        eventSource.onerror = err => observer.error(err);
        return () => {
          eventSource.close();
        };
      })
    );
  }

  static _transformResponse(data) {
    if (!data) {
      return data;
    }
    const json = JSON.parse(data);
    if (json instanceof Array) {
      const applications = json.map(j => new Application(j));
      return sortBy(applications, [item => item.name]);
    }
    return new Application(json);
  }
}

export default Application;
