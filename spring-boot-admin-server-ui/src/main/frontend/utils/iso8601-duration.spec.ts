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
import { describe, expect, test } from 'vitest';

import { toMilliseconds } from '@/utils/iso8601-duration';

describe('iso8601-duration', () => {
  test.each([
    [1.023, 0, 0, 0, 0, 1_023],
    [1.023, 1, 1, 0, 0, 3_661_023],
    [1.023, 1, 1, 1, 0, 90_061_023],
    [1.023, 1, 1, 1, 1, 694_861_023],
  ])(
    'should return the miliseconds of a duration object with %n seconds, %n minutes, %n hours, %n days, %n weeks',
    (seconds, minutes, hours, days, weeks, expected) => {
      const duration = {
        seconds,
        minutes,
        hours,
        days,
        weeks,
      };

      const milliseconds = toMilliseconds(duration);

      expect(milliseconds).toBeCloseTo(expected);
    },
  );
});
