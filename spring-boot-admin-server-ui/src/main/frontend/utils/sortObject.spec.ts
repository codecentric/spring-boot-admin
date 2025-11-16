import { describe, expect, it } from 'vitest';

import { sortObject } from './sortObject';

describe('sortObject', () => {
  it('sorts a flat object alphabetically by keys', () => {
    const input = { b: 2, a: 1, c: 3 };
    const output = sortObject(input);
    expect(Object.keys(output)).toEqual(['a', 'b', 'c']);
    expect(output).toEqual({ a: 1, b: 2, c: 3 });
  });

  it('sorts nested objects recursively', () => {
    const input = { z: 1, a: { d: 4, b: 2, c: 3 }, b: 2 };
    const output = sortObject(input);
    expect(Object.keys(output)).toEqual(['a', 'b', 'z']);
    expect(Object.keys(output.a)).toEqual(['b', 'c', 'd']);
    expect(output).toEqual({ a: { b: 2, c: 3, d: 4 }, b: 2, z: 1 });
  });

  it('leaves arrays unchanged', () => {
    const input = { arr: [3, 1, 2], b: 2, a: 1 };
    const output = sortObject(input);
    expect(output.arr).toEqual([3, 1, 2]);
    expect(Object.keys(output)).toEqual(['a', 'arr', 'b']);
  });

  it('returns empty objects unchanged', () => {
    const input = {};
    const output = sortObject(input);
    expect(output).toEqual({});
  });

  it('returns primitive values unchanged', () => {
    expect(sortObject(42)).toBe(42);
    expect(sortObject('foo')).toBe('foo');
    expect(sortObject(null)).toBe(null);
    expect(sortObject(undefined)).toBe(undefined);
  });
});
