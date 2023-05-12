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

export function truncateJavaType(javaType) {
  return javaType.replace(/java\.[^A-Z]*/g, '');
}

/**
 * Truncates the given package name to the given length.
 *
 * It works similar to {@link https://logback.qos.ch/manual/layouts.html#conversionWord}
 *
 * @param javaType
 * @param length
 * @returns {string|*}
 */
export function truncatePackageName(javaType, length) {
  const split = javaType.split('.');
  if (length > 0) {
    const clazzName = split.pop();

    let shortened;
    for (let i = 0; i <= split.length; i++) {
      shortened = [
        ...[...split].splice(0, i).map((p) => p.charAt(0)),
        ...[...split].splice(i),
        clazzName,
      ].join('.');

      if (shortened.length <= length) {
        return shortened;
      }
    }

    return shortened;
  } else {
    return split?.pop();
  }
}
