/*
 * Copyright 2014-2019 the original author or authors.
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

import Application from '@/services/application';
import {EMPTY} from '@/utils/rxjs';
import Store from './store';

jest.mock('@/services/application');
jest.setTimeout(500);

describe('store', () => {
  describe('given no registered applications', function () {
    Application.list = jest.fn(() => EMPTY);
    Application.getStream = jest.fn(() => EMPTY);

    it('it should emit a connected event after start', async () => {
      const store = new Store();

      const connectedEvent = new Promise(resolve => {
        store.addEventListener('connected', resolve)
      });

      store.start();
      await connectedEvent;
      store.stop();
    });

  });
});
