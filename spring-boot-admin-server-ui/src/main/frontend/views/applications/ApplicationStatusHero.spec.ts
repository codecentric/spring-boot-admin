import { screen } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Ref, ref } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import Application from '@/services/application';
import Instance from '@/services/instance';
import { render } from '@/test-utils';
import ApplicationStatusHero from '@/views/applications/ApplicationStatusHero.vue';

vi.mock('@/composables/useApplicationStore', () => ({
  useApplicationStore: vi.fn(),
}));

describe('ApplicationStatusHero', () => {
  let applications: Ref<Application[]>;

  beforeEach(async () => {
    applications = ref([]);

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    useApplicationStore.mockReturnValue({
      applicationsInitialized: ref(true),
      applications,
      error: ref(null),
    });
  });

  it('shows "all up" when all are up', () => {
    applications.value = [
      new Application({
        name: 'Test Application',
        statusTimestamp: Date.now(),
        instances: [
          new Instance({ id: '4711', statusInfo: { status: 'UP' } }),
          new Instance({ id: '4712', statusInfo: { status: 'UP' } }),
        ],
      }),
    ];

    render(ApplicationStatusHero);

    expect(screen.getByText('all up')).toBeVisible();
  });

  it('shows "Some instances are down" when one is down', () => {
    applications.value = [
      new Application({
        name: 'Test Application',
        statusTimestamp: Date.now(),
        instances: [
          new Instance({ id: '4711', statusInfo: { status: 'UP' } }),
          new Instance({ id: '4712', statusInfo: { status: 'DOWN' } }),
        ],
      }),
    ];

    render(ApplicationStatusHero);

    expect(screen.getByText('Some instances are down')).toBeVisible();
  });

  it('shows "up but unknow" when one is unknown', () => {
    applications.value = [
      new Application({
        name: 'Test Application',
        statusTimestamp: Date.now(),
        instances: [
          new Instance({ id: '4711', statusInfo: { status: 'UP' } }),
          new Instance({ id: '4712', statusInfo: { status: 'UNKNOWN' } }),
        ],
      }),
    ];

    render(ApplicationStatusHero);

    expect(
      screen.getByText('Up, but some instances are unknown'),
    ).toBeVisible();
  });
});
