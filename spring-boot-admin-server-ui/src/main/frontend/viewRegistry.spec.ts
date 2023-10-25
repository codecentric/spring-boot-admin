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
import { describe, expect, it } from 'vitest';

import ViewRegistry from './viewRegistry';

import sbaConfig from '@/sba-config';

describe('viewRegistry', () => {
  it('should replace the existing one', async () => {
    const viewRegistry = new ViewRegistry();

    viewRegistry.addView(
      ...[
        { name: 'view', group: 'group', path: '' },
        { name: 'duplicateView', group: 'group' },
        { name: 'duplicateView', group: 'group' },
      ],
    );

    expect(viewRegistry.views).toHaveLength(2);
  });

  it('should create a redirect based on path', () => {
    const viewRegistry = new ViewRegistry();

    viewRegistry.addRedirect('/', 'asString');
    viewRegistry.addRedirect('/', { name: 'asObject' });

    expect(viewRegistry.routes).toContainEqual(
      expect.objectContaining({
        path: '/',
        redirect: { name: 'asString' },
      }),
    );
    expect(viewRegistry.routes).toContainEqual(
      expect.objectContaining({
        path: '/',
        redirect: { name: 'asObject' },
      }),
    );
  });

  it('hide or show views depending on their settings', () => {
    sbaConfig.uiSettings.viewSettings = [
      { name: 'disabledView', enabled: false },
      { name: 'explicitlyEnabledView', enabled: true },
    ];

    const viewRegistry = new ViewRegistry();
    viewRegistry.addView(
      ...[
        { name: 'disabledView', group: 'group' },
        { name: 'explicitlyEnabledView', group: 'group' },
        { name: 'implicitlyEnabledView', group: 'group' },
      ],
    );

    const disabledView = viewRegistry.getViewByName('disabledView');
    expect(disabledView).toBeDefined();
    expect(disabledView.isEnabled()).toBeFalsy();

    const implicitlyEnabledView = viewRegistry.getViewByName(
      'implicitlyEnabledView',
    );
    expect(implicitlyEnabledView).toBeDefined();
    expect(implicitlyEnabledView.isEnabled()).toBeTruthy();

    const explicitlyEnabledView = viewRegistry.getViewByName(
      'explicitlyEnabledView',
    );
    expect(explicitlyEnabledView).toBeDefined();
    expect(explicitlyEnabledView.isEnabled()).toBeTruthy();
  });

  it('should render a translated label', () => {
    const viewRegistry = new ViewRegistry();
    viewRegistry.addView(...[{ path: 'parent', label: 'parent.label' }]);

    expect(viewRegistry.views[0].handle.render).toBeDefined();
  });

  it('derives name from parent and path', () => {
    const viewRegistry = new ViewRegistry();
    viewRegistry.addView(
      ...[{ path: 'parent' }, { parent: 'parent', path: 'path' }],
    );

    expect(viewRegistry.views).toContainEqual(
      expect.objectContaining({ name: 'parent' }),
    );
    expect(viewRegistry.views).toContainEqual(
      expect.objectContaining({ name: 'parent/path' }),
    );
  });

  it('parent/child routes are generated correctly', () => {
    const viewRegistry = new ViewRegistry();
    viewRegistry.addView(
      ...[
        { path: 'parent', component: {} },
        { parent: 'parent', path: 'path', component: {} },
      ],
    );

    expect(viewRegistry.routes).toContainEqual(
      expect.objectContaining({
        name: 'parent',
      }),
    );
    expect(viewRegistry.routes[0].children).toContainEqual(
      expect.objectContaining({
        name: 'parent/path',
      }),
    );
  });
});
