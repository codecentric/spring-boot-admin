/*
 * Copyright 2014-2020 the original author or authors.
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

import { convertBody, hasMatchingContentType } from './application';

describe('hasMatchingContentType', () => {
  it('should match content-type', () => {
    const matches = hasMatchingContentType('application/json;charset=UTF-8', [
      'application/vnd.spring-boot.actuator.v1+json',
      'application/json',
    ]);
    expect(matches).toBe(true);
  });

  it('should not match content-types', () => {
    const matches = hasMatchingContentType('application/html;charset=UTF-8', [
      'application/vnd.spring-boot.actuator.v1+json',
      'application/json',
    ]);
    expect(matches).toBe(false);
  });

  it('should not match undefined', () => {
    const matches = hasMatchingContentType(undefined, [
      'application/vnd.spring-boot.actuator.v1+json',
      'application/json',
    ]);
    expect(matches).toBe(false);
  });
});

describe('convertBody', () => {
  it('should not convert empty responses', () => {
    expect(convertBody([])).toEqual([]);
  });

  it('should not convert empty body', () => {
    expect(convertBody([{}])).toEqual([{}]);
  });

  it('should not convert non-json body', () => {
    expect(
      convertBody([{ body: 'foobar', contentType: 'text/plain' }]),
    ).toEqual([{ body: 'foobar', contentType: 'text/plain' }]);
  });

  it('should convert json body', () => {
    expect(
      convertBody([
        { body: '{"foo": "bar"}', contentType: 'application/json' },
      ]),
    ).toEqual([{ body: { foo: 'bar' }, contentType: 'application/json' }]);
  });
});
