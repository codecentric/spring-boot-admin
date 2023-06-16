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
import { isEmpty, isObject } from 'lodash-es';

const nonEmptyComplexValue = (value) =>
  (Array.isArray(value) || isObject(value)) && !isEmpty(value);

const objToString = (obj, indent = '') => {
  if (Array.isArray(obj)) {
    if (isEmpty(obj)) {
      return indent + '[]';
    }

    return obj
      .map((value) => {
        if (nonEmptyComplexValue(value)) {
          return `${indent}-\n${objToString(value, indent + '  ')}`;
        }
        return `${indent}- ${objToString(value, '')}`;
      })
      .join('\n');
  }

  if (isObject(obj)) {
    if (isEmpty(obj)) {
      return indent + '{}';
    }

    return Object.entries(obj)
      .map(([key, value]) => {
        if (nonEmptyComplexValue(value)) {
          return `${indent}${key}:\n${objToString(value, indent + '  ')}`;
        }
        return `${indent}${key}:${objToString(value, ' ')}`;
      })
      .join('\n');
  }

  if (obj === null) {
    return indent + 'null';
  }

  if (typeof obj === 'undefined' || obj === '') {
    return '';
  }

  return indent + obj;
};

export default objToString;
