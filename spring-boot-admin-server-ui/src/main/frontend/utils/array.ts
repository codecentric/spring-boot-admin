/*
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

/**
 * Adds values to an array and recursively flattens nested arrays.
 * Ignores null and undefined values.
 *
 * @example
 *   const arr: number[] = [];
 *   pushFlattened(arr, 1, [2, [3, null]], undefined, 4);
 *   arr = [1, 2, 3, 4]
 *
 * @template T Type of array elements
 * @param {T[]} target Target array to which values are added
 * @param {...(T | T[] | null | undefined)} values Values or arrays of values to add and flatten
 * @returns {T[]} The target array with added and flattened values
 */
export const pushFlattened = <T>(
  target: T[],
  ...values: (T | T[] | null | undefined)[]
): T[] => {
  for (const value of values) {
    if (Array.isArray(value)) {
      // recursively flatten
      pushFlattened(target, ...value);
    } else if (value !== null && value !== undefined) {
      target.push(value as T);
    }
  }
  return target;
};
