import { describe, expect, it } from 'vitest';

import { deduplicateInstanceEvents } from './deduplicate-events';

import {
  InstanceEvent,
  InstanceEventType,
} from '@/views/journal/InstanceEvent';

const createEvent = (
  instance: string,
  version: number,
  type = InstanceEventType.REGISTERED,
) =>
  new InstanceEvent({
    instance,
    version,
    type,
    timestamp: '2024-01-01T10:00:00Z',
    registration: { name: instance },
  });

describe('deduplicateInstanceEvents', () => {
  it('removes events with identical instance, type and version', () => {
    const events = [
      createEvent('instance-1', 1),
      createEvent('instance-1', 1, InstanceEventType.DEREGISTERED),
      createEvent('instance-2', 3),
      createEvent('instance-2', 3),
      createEvent('instance-3', 2),
      createEvent('instance-3', 2, InstanceEventType.INFO_CHANGED),
      createEvent('instance-3', 2, InstanceEventType.INFO_CHANGED),
    ];

    const result = deduplicateInstanceEvents(events);

    expect(result).toHaveLength(5);
    expect(result.map((event) => event.key)).toEqual([
      'instance-1-1-REGISTERED-1704103200000',
      'instance-1-1-DEREGISTERED-1704103200000',
      'instance-2-3-REGISTERED-1704103200000',
      'instance-3-2-REGISTERED-1704103200000',
      'instance-3-2-INFO_CHANGED-1704103200000',
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
      'instance-1-2-REGISTERED-1704103200000',
      'instance-2-1-REGISTERED-1704103200000',
      'instance-3-4-REGISTERED-1704103200000',
    ]);
  });
});
