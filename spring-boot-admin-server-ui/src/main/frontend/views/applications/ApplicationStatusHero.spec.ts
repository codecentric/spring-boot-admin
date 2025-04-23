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
    instance1Status | instance2Status | expectedMessage                          | expectedIcon
    ${'UP'}         | ${'UP'}         | ${'all up'}                              | ${'check-circle'}
    ${'OFFLINE'}    | ${'OFFLINE'}    | ${'all down'}                            | ${'minus-circle'}
    ${'UNKNOWN'}    | ${'UNKNOWN'}    | ${'all in unknown state'}                | ${'question-circle'}
    ${'UP'}         | ${'UNKNOWN'}    | ${'some instances are in unknown state'} | ${'question-circle'}
    ${'UP'}         | ${'DOWN'}       | ${'some instances are down'}             | ${'minus-circle'}
    ${'UP'}         | ${'OFFLINE'}    | ${'some instances are down'}             | ${'minus-circle'}
    ${'DOWN'}       | ${'UNKNOWN'}    | ${'some instances are down'}             | ${'minus-circle'}
    ${'DOWN'}       | ${'OFFLINE'}    | ${'all down'}                            | ${'minus-circle'}
    ${'OFFLINE'}    | ${'UP'}         | ${'some instances are down'}             | ${'minus-circle'}
  `(
    '`$expectedMessage` is shown when `$instance1Status` and `$instance2Status`',
    ({ instance1Status, instance2Status, expectedMessage, expectedIcon }) => {
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

      render(ApplicationStatusHero, {
        global: {
          stubs: {
            'font-awesome-icon': {
              template: '<svg data-testid="icon" />',
            },
          },
        },
      });

      expect(screen.getByTestId('icon')).toHaveAttribute('icon', expectedIcon);
      expect(screen.getByText(expectedMessage)).toBeVisible();
    },
  );
});
