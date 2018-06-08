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


import axios from '@/utils/axios';
import uri from '@/utils/uri';
import moment from 'moment';

class NotificationFilter {
  constructor({expiry, ...filter}) {
    Object.assign(this, filter);
    this.expiry = expiry ? moment(expiry) : null;
  }

  affects(obj) {
    if (!obj) {
      return false;
    }

    if (this.isApplicationFilter) {
      return this.applicationName === obj.name;
    }

    if (this.isInstanceFilter) {
      return this.instanceId === obj.id;
    }

    return false;
  }

  get isApplicationFilter() {
    return this.hasOwnProperty('applicationName');
  }

  get isInstanceFilter() {
    return this.hasOwnProperty('instanceId');
  }

  async delete() {
    return axios.delete(uri`notifications/filters/${this.id}`);
  }

  static async isSupported() {
    try {
      const response = await axios.options('notifications/filters');
      return response.headers['allow'] && response.headers['allow'].includes('POST');
    } catch (error) {
      return false;
    }
  }

  static async getFilters() {
    return axios.get('notifications/filters', {
      transformResponse: NotificationFilter._transformResponse
    });
  }

  static async addFilter(object, ttl) {
    const params = {ttl};
    if (object.hasOwnProperty('name')) {
      params.applicationName = object.name;
    } else if (object.hasOwnProperty('id')) {
      params.instanceId = object.id;
    }
    return axios.post('notifications/filters', null, {
      params,
      transformResponse: NotificationFilter._transformResponse
    });
  }

  static _transformResponse(data) {
    if (!data) {
      return data;
    }
    const json = JSON.parse(data);
    if (json instanceof Array) {
      return json.map(NotificationFilter._toNotificationFilters).filter(f => !f.expired);
    }
    return NotificationFilter._toNotificationFilters(json);
  }

  static _toNotificationFilters(notificationFilter) {
    return new NotificationFilter(notificationFilter);
  }

}

export default NotificationFilter;

