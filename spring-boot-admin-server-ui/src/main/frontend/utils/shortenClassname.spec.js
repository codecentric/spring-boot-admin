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

import shortenClassname from './shortenClassname';

describe('shortenClassname', () => {

  it('should shorten when too long', () => {
    expect(shortenClassname('de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration', 40)).toBe('d.c.b.a.s.config.AdminServerAutoConfiguration')
  });

  it('should not shorten when string is small enough', () => {
    expect(shortenClassname('de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration', 300)).toBe('de.codecentric.boot.admin.server.config.AdminServerAutoConfiguration')
  });

  it('should not shorten when no package is present', () => {
    expect(shortenClassname('AdminServerAutoConfiguration', 1)).toBe('AdminServerAutoConfiguration')
  });
});
