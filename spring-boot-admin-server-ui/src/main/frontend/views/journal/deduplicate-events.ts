import { InstanceEvent } from '@/views/journal/InstanceEvent';

/**
 * Removes duplicate instance events from an array based on their key property.
 *
 * This function filters an array of InstanceEvent objects, keeping only the first occurrence
 * of each unique event key. Subsequent events with the same key are filtered out.
 *
 * @param events - Array of InstanceEvent objects to deduplicate
 * @returns A new array containing only unique events (by key), preserving the order of first occurrence
 *
 * @example
 * const events = [
 *   { key: 'event1', ... },
 *   { key: 'event2', ... },
 *   { key: 'event1', ... } // duplicate
 * ];
 * const unique = deduplicateInstanceEvents(events);
 * // Returns first two events only
 */
export function deduplicateInstanceEvents(events: InstanceEvent[]) {
  const seen = new Set<string>();
  return events.filter((event) => {
    if (seen.has(event.key)) {
      return false;
    }
    seen.add(event.key);
    return true;
  });
}
