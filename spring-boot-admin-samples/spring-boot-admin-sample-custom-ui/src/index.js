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

import custom from './custom';
import customEndpoint from './custom-endpoint';

/* global SBA */
SBA.extensions.push({
  install({viewRegistry}) {
    viewRegistry.addView({
      name: 'custom',
      path: '/custom',
      component: custom,
      props: true,
      label: 'Custom',
      order: 1000,
    });

    viewRegistry.addView({
      name: 'instances/custom',
      parent: 'instances',
      path: 'custom',
      component: customEndpoint,
      props: true,
      label: 'Custom',
      order: 1000,
      isEnabled: ({instance}) => instance.hasEndpoint('custom')
    });
  }
});
