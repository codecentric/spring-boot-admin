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

  it.each`
    instance1Status | instance2Status | expectedMessage
    ${'UP'}         | ${'UP'}         | ${'all up'}
    ${'OFFLINE'}    | ${'OFFLINE'}    | ${'all down'}
    ${'UNKNOWN'}    | ${'UNKNOWN'}    | ${'all in unknown state'}
    ${'UP'}         | ${'UNKNOWN'}    | ${'some instances are in unknown state'}
    ${'UP'}         | ${'DOWN'}       | ${'some instances are down'}
    ${'UP'}         | ${'OFFLINE'}    | ${'some instances are down'}
    ${'DOWN'}       | ${'UNKNOWN'}    | ${'some instances are down'}
    ${'DOWN'}       | ${'OFFLINE'}    | ${'all down'}
    ${'OFFLINE'}    | ${'UP'}         | ${'some instances are down'}
  `(
    '`$expectedMessage` is shown when `$instance1Status` and `$instance2Status`',
    ({ instance1Status, instance2Status, expectedMessage }) => {
      applications.value = [
        new Application({
          name: 'Test Application',
          statusTimestamp: Date.now(),
          instances: [
            new Instance({
              id: '4711',
              statusInfo: { status: instance1Status },
            }),
            new Instance({
              id: '4712',
              statusInfo: { status: instance2Status },
            }),
          ],
        }),
      ];

      render(ApplicationStatusHero);

      expect(screen.getByText(expectedMessage)).toBeVisible();
    },
  );
});
