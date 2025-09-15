/*!
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

export class InstanceEvent {
  public readonly instance: string;
  public readonly version: string;
  public readonly type: string;
  public readonly timestamp: Date;
  public readonly payload: any;

  constructor({ instance, version, type, timestamp, ...payload }) {
    this.instance = instance;
    this.version = version;
    this.type = type;
    this.timestamp = new Date(timestamp);
    this.payload = payload;
  }

  get key() {
    return `${this.instance}-${this.version}`;
  }
}
InstanceEvent.STATUS_CHANGED = 'STATUS_CHANGED';
InstanceEvent.REGISTERED = 'REGISTERED';
InstanceEvent.DEREGISTERED = 'DEREGISTERED';
InstanceEvent.REGISTRATION_UPDATED = 'REGISTRATION_UPDATED';
InstanceEvent.INFO_CHANGED = 'INFO_CHANGED';
InstanceEvent.ENDPOINTS_DETECTED = 'ENDPOINTS_DETECTED';
