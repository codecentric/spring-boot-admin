import { screen, waitFor } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ref } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import Application from '@/services/application';
import Instance from '@/services/instance';
import { render } from '@/test-utils';
import Wallboard from '@/views/wallboard/index.vue';

vi.mock('@/composables/useApplicationStore', () => ({
  useApplicationStore: vi.fn(),
}));

describe('Wallboard', () => {
  let applicationsInitialized;
  let applications;
  let error;

  beforeEach(async () => {
    applicationsInitialized = ref(false);
    applications = ref([]);
    error = ref(null);

    useApplicationStore.mockReturnValue({
      applicationsInitialized,
      applications,
      error,
    });

    await render(Wallboard);
  });

  it('shows message when applications list is loaded', async () => {
    screen.findByText('applications.loading_applications');
  });

  it('shows application', async () => {
    applicationsInitialized.value = true;
    applications.value = [
      new Application({
        name: 'Test Application',
        statusTimestamp: Date.now(),
        instances: [new Instance({ id: '4711' })],
      }),
    ];
    await waitFor(() => {
      expect(screen.getByText('Test Application')).toBeVisible();
    });
  });

  it('shows error, when connection is lost', async () => {
    applicationsInitialized.value = true;
    applications.value = [];
    error.value = new Error('Connection lost');
    await waitFor(() => {
      expect(screen.getByText('Server connection failed.')).toBeVisible();
    });
  });
});
