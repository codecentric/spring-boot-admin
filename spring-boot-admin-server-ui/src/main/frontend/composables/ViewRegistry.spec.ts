import { beforeAll, describe, expect, it } from 'vitest';

import {
  createViewRegistry,
  useViewRegistry,
} from '@/composables/ViewRegistry';
import ViewRegistry from '@/viewRegistry';

describe('useViewRegistry', () => {
  let viewRegistry: ViewRegistry;

  beforeAll(() => {
    viewRegistry = createViewRegistry();
    // extensions are loaded as <script> tags, so they register views after the router exists
    viewRegistry.createRouter();
  });

  it('adds a route for a top-level view registered after the router was created', () => {
    const { addView } = useViewRegistry();

    addView({ name: 'custom', path: '/custom', component: {} });

    expect(viewRegistry.router.hasRoute('custom')).toBe(true);
  });

  it('adds a child route for a view registered after the router was created', () => {
    const { addView } = useViewRegistry();

    addView({ name: 'custom-parent', path: '/custom-parent', component: {} });
    addView({
      name: 'custom-parent/child',
      parent: 'custom-parent',
      path: 'child',
      component: {},
    });

    expect(viewRegistry.router.resolve('/custom-parent/child').name).toBe(
      'custom-parent/child',
    );
  });
});
