/*!
 * Copyright 2014-2025 the original author or authors.
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
import { describe, expect, it } from 'vitest';

import { InstanceEvent } from '@/views/journal/InstanceEvent';

import { deduplicateInstanceEvents } from './deduplicate-events';

const createEvent = (instance: string, version: number) =>
  new InstanceEvent({
    instance,
    version,
    type: 'REGISTERED',
    timestamp: '2024-01-01T10:00:00Z',
    registration: { name: instance },
  });

describe('deduplicateInstanceEvents', () => {
  it('removes events with identical instance and version', () => {
    const events = [
      createEvent('instance-1', 1),
      createEvent('instance-1', 1),
      createEvent('instance-2', 3),
      createEvent('instance-2', 3),
      createEvent('instance-3', 2),
    ];

    const result = deduplicateInstanceEvents(events);

    expect(result).toHaveLength(3);
    expect(result.map((event) => event.key)).toEqual([
      'instance-1-1',
      'instance-2-3',
      'instance-3-2',
    ]);
  });

  it('preserves the order of the first occurrences', () => {
    const events = [
      createEvent('instance-1', 2),
      createEvent('instance-2', 1),
      createEvent('instance-1', 2),
      createEvent('instance-3', 4),
    ];

    const result = deduplicateInstanceEvents(events);

    expect(result.map((event) => event.key)).toEqual([
      'instance-1-2',
      'instance-2-1',
      'instance-3-4',
    ]);
  });
});
