/**
 * Converts a flat object with dot notation keys into a nested POJO.
 *
 * Supports two modes:
 * - **STRICT** (default): Later values overwrite earlier values for the same path.
 * - **LAX**: Resolves conflicts between primitive values and nested objects by using
 *   a sentinel key `__` to preserve both values.
 *
 * @param input - The flat object to transform.
 * @param mode - Transformation mode: 'STRICT' or 'LAX'. Defaults to 'STRICT'.
 * @returns The nested POJO.
 *
 * @example
 * // Basic transformation with nested objects and arrays
 * transformToJSON({
 *   'user.name': 'Alice',
 *   'user.address.street': 'Main St',
 *   'user.address.zip': '12345',
 *   'items.0.id': 1,
 *   'items.0.name': 'Item 1',
 *   'items.1.id': 2,
 *   'items.1.name': 'Item 2'
 * });
 * // Returns:
 * // {
 * //   user: {
 * //     name: 'Alice',
 * //     address: { street: 'Main St', zip: '12345' }
 * //   },
 * //   items: [
 * //     { id: 1, name: 'Item 1' },
 * //     { id: 2, name: 'Item 2' }
 * //   ]
 * // }
 *
 * @example
 * // LAX mode: Conflict resolution with sentinel key '__'
 * // When a property is both a primitive and has nested properties
 * transformToJSON({
 *   'my-service': 'simple-value',
 *   'my-service.kubernetes': 'namespace'
 * }, 'LAX');
 * // Returns:
 * // {
 * //   'my-service': {
 * //     __: 'simple-value',        // Original primitive value preserved
 * //     kubernetes: 'namespace'     // Nested property added
 * //   }
 * // }
 *
 * @example
 * // LAX mode: Reverse order (deep path first, then shallow)
 * transformToJSON({
 *   'config.server.port': '8080',
 *   'config': 'default-config'
 * }, 'LAX');
 * // Returns:
 * // {
 * //   config: {
 * //     __: 'default-config',       // Later primitive value preserved
 * //     server: { port: '8080' }    // Earlier nested structure kept
 * //   }
 * // }
 *
 * @remarks
 * - The sentinel key `__` is only used in LAX mode when conflicts occur.
 * - Numeric keys (e.g., '0', '1') create arrays automatically.
 * - In STRICT mode, the last value for a given path wins.
 * - Avoid using `__` as a key in your input data when using LAX mode.
 */
export function transformToJSON(
  input: Record<string, any>,
  mode: 'STRICT' | 'LAX' = 'STRICT',
): any {
  const result: any = {};
  const isPrimitive = (val: any) =>
    val === null || ['string', 'number', 'boolean'].includes(typeof val);

  for (const [flatKey, value] of Object.entries(input)) {
    const parts = flatKey.split('.');
    let current: any = result;

    for (let i = 0; i < parts.length; i++) {
      const part = parts[i];
      const nextPart = parts[i + 1];
      const isNextArrayIndex = nextPart !== undefined && /^\d+$/.test(nextPart);

      const isLast = i === parts.length - 1;

      if (isLast) {
        // Final segment: assign the value
        if (mode === 'LAX') {
          const existing = current[part];
          // Handle conflict: existing object receives primitive value at root level
          if (existing && typeof existing === 'object') {
            if (!('__' in existing)) {
              existing.__ = value;
            }
            continue; // Preserve existing nested structure
          }
        }

        current[part] = value;
      } else {
        // Intermediate segment
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
          // Object key: navigate or create path
          if (!current[part]) {
            current[part] = isNextArrayIndex ? [] : {};
          } else if (mode === 'LAX' && isPrimitive(current[part])) {
            current[part] = { __: current[part] };
          }
          current = current[part];
        }
      }
    }
  }
  return result;
}
