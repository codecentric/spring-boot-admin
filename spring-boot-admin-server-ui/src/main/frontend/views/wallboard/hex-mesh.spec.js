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

import * as hm from './hex-mesh';

describe('calcLayout', () => {

  it('should calculate optimum layout for 12 in 1594x879', () => {
    const result = hm.calcLayout(12, 1594, 879);

    expect(result).toEqual({
      rows: 3,
      cols: 5,
      sideLength: 175.8
    });
  });

  it('should calculate optimum layout for 1 in 100x100', () => {
    const result = hm.calcLayout(1, 100, 100);

    expect(result).toEqual({
      rows: 1,
      cols: 1,
      sideLength: 50
    });
  });

});
