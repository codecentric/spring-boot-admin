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
import {
  IInstanceEvent,
  InstanceEventType,
} from '@/views/journal/InstanceEvent';

/**
 * Extracts application names from instance events.
 *
 * @param events
 */
export const getApplicationNamesByInstanceId = (events: IInstanceEvent[]) => {
  return events
    .filter(
      (event) =>
        event.type === InstanceEventType.REGISTERED ||
        event.type === InstanceEventType.REGISTRATION_UPDATED,
    )
    .sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime())
    .reduceRight((names, event) => {
      names[event.instance] = event.registration?.name;
      return names;
    }, {});
};

export const getApplicationNames = (events: IInstanceEvent[]): string[] => {
  const applicationNames = events
    .filter(
      (event) =>
        event.type === InstanceEventType.REGISTERED ||
        event.type === InstanceEventType.REGISTRATION_UPDATED,
    )
    .sort((a, b) => b.timestamp.getTime() - a.timestamp.getTime())
    .map((event) => event.registration?.name)
    .sort((a, b) => a.localeCompare(b));

  return [...new Set(applicationNames)];
};
