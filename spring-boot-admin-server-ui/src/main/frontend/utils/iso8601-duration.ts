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

export { parse } from 'iso8601-duration';

/**
 * Convert ISO8601 duration object to milliseconds
 *
 * Hint: Years and months are ignored.
 * Calculating based on JavaScript date is too imprecise.
 *
 * @param {Object} duration - The duration object
 * @return {Number}
 */
export const toMilliseconds = (duration) => {
  let result = duration.seconds;
  result += duration.minutes * 60;
  result += duration.hours * 60 * 60;
  result += duration.days * 60 * 60 * 24;
  result += duration.weeks * 60 * 60 * 24 * 7;

  return result * 1000;
};
