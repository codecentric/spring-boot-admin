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

/**
 * URI template tag function that encodes interpolated values using encodeURIComponent.
 *
 * Usage example:
 *   const userId = 'john/doe';
 *   const url = uri`/api/users/${userId}/profile`;
 *   // url === "/api/users/john%2Fdoe/profile"
 *
 * @param strings Template string array
 * @param values Interpolated values to encode
 * @returns Encoded URI string
 */
export default (strings, ...values) => {
  let result = strings[0];
  for (let i = 0; i < values.length; ++i) {
    result += encodeURIComponent(values[i]) + strings[i + 1];
  }
  return result;
};
