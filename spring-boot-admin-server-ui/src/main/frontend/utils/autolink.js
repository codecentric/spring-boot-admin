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

import _Autolinker from 'autolinker';

export const defaults = {
  urls: {
    schemeMatches: true,
    wwwMatches: false,
    tldMatches: false
  },
  email: false,
  phone: false,
  mention: false,
  hashtag: false,

  stripPrefix: false,
  stripTrailingSlash: false,
  newWindow: true,

  truncate: {
    length: 0,
    location: 'smart'
  },

  className: ''
};
const autolinker = new _Autolinker(defaults);

export default s => autolinker.link(s);
export function Autolink(cfg) {
  this.autolinker = new _Autolinker({...defaults, ...cfg});
  return s => this.autolinker.link(s)
}
