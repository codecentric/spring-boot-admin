/**
 * Converts a flat object with dot notation keys into a nested POJO.
 *
 * @param input - The flat object to transform.
 * @returns The nested POJO.
 *
 * @example
 * transformToJSON({
 *   'user.name': 'Alice',
 *   'user.address.street': 'Main St',
 *   'user.address.zip': '12345',
 *   'items.0.id': 1,
 *   'items.0.name': 'Item 1',
 *   'items.1.id': 2,
 *   'items.1.name': 'Item 2'
 * });
 * Returns:
 * {
 *   user: {
 *     name: 'Alice',
 *     address: { street: 'Main St', zip: '12345' }
 *   },
 *   items: [
 *     { id: 1, name: 'Item 1' },
 *     { id: 2, name: 'Item 2' }
 *   ]
 * }
 */
export function transformToJSON(input: Record<string, any>): any {
  const result: any = {};
  for (const [flatKey, value] of Object.entries(input)) {
    const parts = flatKey.split('.');
    let current = result;
    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];
      const nextPart = parts[i + 1];
      const isNextArrayIndex = nextPart !== undefined && /^\d+$/.test(nextPart);
      if (i === parts.length - 1) {
        current[part] = value;
      } else {
        if (/^\d+$/.test(part)) {
          const idx = Number(part);
          if (!Array.isArray(current)) {
            const arr: any[] = [];
            Object.assign(arr, current);
            current = arr;
          }
          if (!current[idx]) current[idx] = isNextArrayIndex ? [] : {};
          current = current[idx];
        } else {
          if (!current[part]) current[part] = isNextArrayIndex ? [] : {};
          current = current[part];
        }
      }
    }
  }
  return result;
}
