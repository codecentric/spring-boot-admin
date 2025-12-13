import { InstanceEvent } from '@/views/journal/InstanceEvent';

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
