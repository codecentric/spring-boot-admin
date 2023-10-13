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
import sbaConfig from '@/sba-config';
import axios from '@/utils/axios';
import uri from '@/utils/uri';
import Application from "@/services/application";
import Instance from "@/services/instance";

export type NotificationFilterProps = {
    id: string,
    applicationName: string,
    instanceId: string,
    expiry: string,
    expired: boolean
}

class NotificationFilter {
    public readonly expired: boolean;
    private readonly id: string;
    private readonly applicationName: string;
    private readonly instanceId: string;

    constructor({id, applicationName, instanceId, expiry, expired, ...filter}: NotificationFilterProps) {
        Object.assign(this, filter);
        this.id = id;
        this.applicationName = applicationName;
        this.instanceId = instanceId;
        this.expired = expired;
    }

    static isSupported() {
        return Boolean(sbaConfig.uiSettings.notificationFilterEnabled);
    }

    static async getFilters() {
        return axios.get('notifications/filters', {
            transformResponse: NotificationFilter._transformResponse,
        });
    }

    static async addFilter(object: Instance | Application, ttl: number) {
        const params = {ttl} as { ttl: number, applicationName?: string; instanceId?: string };
        if (object instanceof Application) {
            params.applicationName = object.name;
        } else if ('id' in object) {
            params.instanceId = object.id;
        }
        return axios.post('notifications/filters', null, {
            params,
            transformResponse: NotificationFilter._transformResponse,
        });
    }

    static _transformResponse(data: any) {
        if (!data) {
            return data;
        }
        const json = JSON.parse(data);
        if (json instanceof Array) {
            return json
                .map((notificationFilter) => new NotificationFilter(notificationFilter))
                .filter((f) => !f.expired);
        }
        return new NotificationFilter(json);
    }

    affects(obj: Instance | Application) {
        if (!obj) {
            return false;
        }

        if (obj instanceof Application) {
            return this.applicationName === obj.name;
        }

        return this.instanceId === obj.id;
    }

    async delete() {
        return axios.delete(uri`notifications/filters/${this.id}`);
    }
}

export default NotificationFilter;
