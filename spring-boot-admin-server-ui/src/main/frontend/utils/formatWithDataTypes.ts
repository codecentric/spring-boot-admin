/*
 * Copyright 2014-2026 the original author or authors.
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
import prettyBytes from 'pretty-bytes';

import { formatDateTime } from './prettyTime';

/**
 * Configuration object mapping property paths to their data types.
 * Keys are dot-notation paths (e.g., 'memory.heap.committed'), values are data type names.
 */
type DataTypeConfig = {
  [key: string]: string;
};

/**
 * Formats object values based on their data types.
 *
 * Converts a proxy or regular object to a raw object and applies formatting
 * to specified properties based on their data types.
 *
 * Supported data types:
 * - 'bytes': Formats numeric byte values to human-readable format (e.g., '1.02 kB').
 *            Only formats non-negative values.
 * - 'date': Formats timestamps or ISO strings to localized date-time strings.
 *
 * @template T - The type of the input object
 * @param input - The object to format. Primitive values are returned unchanged.
 * @param config - Optional configuration mapping property paths to data types.
 *                 Uses dot notation for nested properties (e.g., 'memory.heap.committed').
 * @returns A new object with formatted values, or the original input if it's not an object.
 *
 * @example
 * const data = { memory: { heap: { committed: 1024 } }, timestamp: 1717243496000 };
 * const formatted = formatWithDataTypes(data, {
 *   'memory.heap.committed': 'bytes',
 *   'timestamp': 'date'
 * });
 * // Returns: { memory: { heap: { committed: '1.02 kB' } }, timestamp: 'Jun 1, 2024, 12:34:56 PM' }
 */
export function formatWithDataTypes<T>(input: T, config?: DataTypeConfig): T {
  if (typeof input !== 'object' || input === null) {
    return input;
  }

  if (!config) {
    return input;
  }

  const result = JSON.parse(JSON.stringify(input)) as any;

  // Process each configured property path
  for (const [path, dataType] of Object.entries(config)) {
    const keys = path.split('.');
    let current = result;

    // Navigate through nested objects to find the parent of the target property
    for (let i = 0; i < keys.length - 1; i++) {
      if (current[keys[i]] === undefined) {
        break;
      }
      current = current[keys[i]];
    }

    // Apply formatting to the target property
    const lastKey = keys[keys.length - 1];
    if (current && current[lastKey] !== undefined) {
      try {
        if (dataType === 'bytes' && current[lastKey] >= 0) {
          current[lastKey] = prettyBytes(current[lastKey]);
        } else if (dataType === 'date') {
          current[lastKey] = formatDateTime(current[lastKey]);
        }
      } catch {
        // Keep original value on error
      }
    }
  }

  return result as T;
}
