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
import { dump } from 'js-yaml';

/**
 * Convert JSON to YAML.
 * @param input  A JSON object or a JSON string.
 * @param options Optional js-yaml dump options.
 * @returns YAML string.
 */
export function objToYaml(
  input: object | string,
  options: Parameters<typeof dump>[1] = {},
): string {
  if (typeof input === 'string') {
    return input;
  }

  return dump(input, {
    noRefs: true,
    indent: 2,
    ...options,
  });
}
