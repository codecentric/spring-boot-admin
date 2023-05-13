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
import { describe, expect, it } from 'vitest';

import {
  truncateJavaType,
  truncatePackageName,
} from '@/views/instances/jolokia/utils';

describe('utils.js', () => {
  it('truncateJavaType', () => {
    expect(truncateJavaType('java.lang.String')).toEqual('String');
  });

  it.each`
    length | expected
    ${0}   | ${'Bar'}
    ${5}   | ${'m.s.s.Bar'}
    ${10}  | ${'m.s.s.Bar'}
    ${15}  | ${'m.s.sample.Bar'}
    ${16}  | ${'m.sub.sample.Bar'}
    ${26}  | ${'mainPackage.sub.sample.Bar'}
  `('truncatePackageName having length $length', ({ expected, length }) => {
    const result = truncatePackageName('mainPackage.sub.sample.Bar', length);
    expect(result).toEqual(expected);
  });
});
