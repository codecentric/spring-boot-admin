/*
 * Copyright 2014-2018 the original author or authors.
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

import objToString from './objToString'

describe('objToString should', () => {

  it('return the input string for normal text', () => {
    const obj = {
      a: 'start',
      b: 1,
      c: true,
      d: [1, 2, [3, 4]],
      e: {
        f: '',
        g: 1,
        h: null,
        i: undefined,
        j: {},
        k: [1],
        l: [{a:1, b:'foo'}, {b:2}]
      }
    };
    const str = `a: start
b: 1
c: true
d:
  - 1
  - 2
  -
    - 3
    - 4
e:
  f: 
  g: 1
  h: null
  i: 
  j: {}
  k:
    - 1
  l:
    -
      a: 1
      b: foo
    -
      b: 2`;
    expect(objToString(obj)).toBe(str);
  });

});
