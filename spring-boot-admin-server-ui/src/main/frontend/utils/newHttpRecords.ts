/** Actuator returns a rolling window. A request can finish after a later-started one. */
export function newHttpRecords<T>() {
  let previous = new Map<string, number>();
  return (records: T[]) => {
    const current = new Map<string, number>();
    const added = records.filter((record) => {
      const key = JSON.stringify(record);
      const count = (current.get(key) ?? 0) + 1;
      current.set(key, count);
      return count > (previous.get(key) ?? 0);
    });
    previous = current;
    return added;
  };
}
