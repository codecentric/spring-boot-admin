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

export interface IInstanceEvent {
  instance: string;
  version: number;
  type: InstanceEventType;
  timestamp: Date;
  [key: string]: any;
}

export class InstanceEvent implements IInstanceEvent {
  public readonly instance: string;
  public readonly version: number;
  public readonly type: InstanceEventType;
  public readonly timestamp: Date;

  constructor({ instance, version, type, timestamp, ...rest }: IInstanceEvent) {
    this.instance = instance;
    this.version = version;
    this.type = type;
    this.timestamp = new Date(timestamp);

    Object.assign(this, rest);
  }

  get key() {
    return `${this.instance}-${this.version}-${this.type}-${this.timestamp.getTime()}`;
  }
}

export enum InstanceEventType {
  STATUS_CHANGED = 'STATUS_CHANGED',
  REGISTERED = 'REGISTERED',
  DEREGISTERED = 'DEREGISTERED',
  REGISTRATION_UPDATED = 'REGISTRATION_UPDATED',
  INFO_CHANGED = 'INFO_CHANGED',
  ENDPOINTS_DETECTED = 'ENDPOINTS_DETECTED',
}
