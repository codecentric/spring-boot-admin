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
import { describe, expect, it } from 'vitest';

import { formatWithDataTypes } from './formatWithDataTypes';

describe('formatWithDataTypes', () => {
  it('returns primitive values unchanged', () => {
    expect(formatWithDataTypes(42)).toBe(42);
    expect(formatWithDataTypes('foo')).toBe('foo');
    expect(formatWithDataTypes(null)).toBe(null);
    expect(formatWithDataTypes(undefined)).toBe(undefined);
  });

  it('returns object unchanged when no config provided', () => {
    const input = { a: 1, b: 2 };
    const output = formatWithDataTypes(input);
    expect(output).toEqual({ a: 1, b: 2 });
  });

  it('applies prettyBytes for bytes datatype', () => {
    const input = { memory: { heap: { committed: 1024 } } };
    const output = formatWithDataTypes(input, {
      'memory.heap.committed': 'bytes',
    });
    expect(output.memory.heap.committed).toBe('1.02 kB');
  });

  it('wraps value with type for non-bytes datatypes', () => {
    const input = { duration: 5000 };
    const output = formatWithDataTypes(input, { duration: 'milliseconds' });
    expect(output.duration).toEqual(5000);
  });

  it('handles multiple config entries', () => {
    const input = {
      memory: { heap: { committed: 2048 } },
      duration: 3000,
    };
    const output = formatWithDataTypes(input, {
      'memory.heap.committed': 'bytes',
      duration: 'milliseconds',
    });
    expect(output.memory.heap.committed).toBe('2.05 kB');
    expect(output.duration).toEqual(3000);
  });

  it('ignores missing keys in config', () => {
    const input = { a: 1 };
    const output = formatWithDataTypes(input, { 'b.c.d': 'bytes' });
    expect(output).toEqual({ a: 1 });
  });

  it('formats date from timestamp', () => {
    const input = { timestamp: 1717243496000 };
    const output = formatWithDataTypes(input, { timestamp: 'date' });
    expect(output.timestamp).toMatch(/01.06.2024, 14:04:56/);
  });

  it('formats date from ISO string', () => {
    const input = { created: '2024-06-01T12:34:56Z' };
    const output = formatWithDataTypes(input, { created: 'date' });
    expect(output.created).toMatch(/01.06.2024, 14:34:56/);
  });

  it('handles formatting errors gracefully', () => {
    const input = { invalidDate: 'not-a-date', invalidBytes: 'not-a-number' };
    const output = formatWithDataTypes(input, {
      invalidDate: 'date',
      invalidBytes: 'bytes',
    });
    expect(output.invalidDate).toBe('not-a-date');
    expect(output.invalidBytes).toBe('not-a-number');
  });

  it('does not format negative bytes', () => {
    const input = { size: -1024 };
    const output = formatWithDataTypes(input, { size: 'bytes' });
    expect(output.size).toBe(-1024);
  });
});
