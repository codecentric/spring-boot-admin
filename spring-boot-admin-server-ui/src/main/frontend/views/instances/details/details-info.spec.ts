import { screen } from '@testing-library/vue';
import { describe, expect, it, vi } from 'vitest';

import DetailsInfo from './details-info.vue';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import { render } from '@/test-utils';

describe('DetailsInfo', () => {
  it('should render info from instance.info', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    render(DetailsInfo, {
      props: { instance },
    });

    // The mock instance has info with 'tags', 'scm-url', 'build-url', 'build' keys
    expect(await screen.findByText('tags')).toBeVisible();
    expect(await screen.findByText('build')).toBeVisible();
  });

  it('should show empty message when info is empty', async () => {
    const application = new Application({
      ...applications[0],
      instances: [
        {
          ...applications[0].instances[0],
          info: {},
        },
      ],
    });
    const instance = application.instances[0];

    render(DetailsInfo, {
      props: { instance },
    });

    expect(
      await screen.findByText('instances.details.info.no_info_provided'),
    ).toBeVisible();
  });

  it('should update when instance prop changes', async () => {
    const application = new Application(applications[0]);
    const instance1 = application.instances[0];

    const { rerender } = render(DetailsInfo, {
      props: { instance: instance1 },
    });

    // Initial render shows info from mock data
    expect(await screen.findByText('tags')).toBeVisible();

    // Create new instance with different info
    const instance2 = new Application({
      ...applications[0],
      instances: [
        {
          ...applications[0].instances[0],
          info: { foo: 'bar' },
        },
      ],
    }).instances[0];

    await rerender({ instance: instance2 });

    expect(await screen.findByText('foo')).toBeVisible();
    expect(await screen.findByText('bar')).toBeVisible();
  });

  it('should show count of info entries', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    render(DetailsInfo, {
      props: { instance },
    });

    // The mock has 4 info keys: tags, scm-url, build-url, build
    // The count appears in the accordion title slot
    expect(await screen.findByText('(4)')).toBeVisible();
  });

  describe('SSE reactive updates', () => {
    it('should never call instance.fetchInfo (no direct actuator calls)', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];

      // Spy on fetchInfo to prove it's never called
      const fetchInfoSpy = vi.spyOn(instance, 'fetchInfo');

      render(DetailsInfo, {
        props: { instance },
      });

      // Wait for component to fully render
      await screen.findByText('tags');

      // Assert fetchInfo was NEVER called - this proves the bug fix
      expect(fetchInfoSpy).not.toHaveBeenCalled();
    });

    it('should reactively update through multiple SSE info changes without HTTP calls', async () => {
      const baseApp = applications[0];

      // 1. Initial SSE event with full info
      const instance1 = new Application(baseApp).instances[0];
      const fetchInfoSpy1 = vi.spyOn(instance1, 'fetchInfo');

      const { rerender } = render(DetailsInfo, {
        props: { instance: instance1 },
      });

      expect(await screen.findByText('tags')).toBeVisible();
      expect(await screen.findByText('build')).toBeVisible();
      expect(fetchInfoSpy1).not.toHaveBeenCalled();

      // 2. SSE event: info changes (simulates INFO_CHANGED event)
      const instance2 = new Application({
        ...baseApp,
        instances: [
          {
            ...baseApp.instances[0],
            version: 4,
            info: {
              app: { name: 'my-service', version: '2.0.0' },
              'git.branch': 'main',
            },
          },
        ],
      }).instances[0];
      const fetchInfoSpy2 = vi.spyOn(instance2, 'fetchInfo');

      await rerender({ instance: instance2 });

      expect(await screen.findByText('app')).toBeVisible();
      expect(await screen.findByText('git.branch')).toBeVisible();
      expect(screen.queryByText('tags')).not.toBeInTheDocument();
      expect(fetchInfoSpy2).not.toHaveBeenCalled();

      // 3. SSE event: info becomes empty
      const instance3 = new Application({
        ...baseApp,
        instances: [
          {
            ...baseApp.instances[0],
            version: 5,
            info: {},
          },
        ],
      }).instances[0];
      const fetchInfoSpy3 = vi.spyOn(instance3, 'fetchInfo');

      await rerender({ instance: instance3 });

      expect(
        await screen.findByText('instances.details.info.no_info_provided'),
      ).toBeVisible();
      expect(fetchInfoSpy3).not.toHaveBeenCalled();
    });
  });
});
