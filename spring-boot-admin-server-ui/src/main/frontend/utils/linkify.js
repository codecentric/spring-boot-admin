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

import linkifyStr from 'linkifyjs/string';

const shorten = (value, max) => {
  if (value.length <= max) {
    //no need to shorten
    return value;
  }

  let url = value.replace(/https?:\/\//, '');
  if (url.length <= max) {
    //just shorten the protocol
    return url;
  }

  const last = url.lastIndexOf('/');
  if (last >= 0 && (url.length - last) < max) {
    //strip out some of the middle parts
    const end = url.substr(last);
    let first = url.lastIndexOf('/', max - 3 - end.length);
    if (first < 0) {
      first = max - 3 - end.length;
    }
    return url.substr(0, first + 1) + '...' + end;
  } else {
    //shorten at the end
    return url.substr(0, max - 3) + '...';
  }
};

export default (input, maxLength) => linkifyStr(input, {
  className: null,
  format(value, type) {
    if (type === 'url') {
      return shorten(value, maxLength || Number.MAX_VALUE)
    }
    return value;
  }
});
