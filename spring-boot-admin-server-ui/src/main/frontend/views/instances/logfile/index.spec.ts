import { screen, waitFor } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import { render } from '@/test-utils';
import LogFile from '@/views/instances/logfile/index.vue';

describe('LogFile', () => {
  beforeEach(async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];

    render(LogFile, {
      props: {
        instance,
      },
    });
  });

  it('disables the wrap lines by default', async () => {
    const checkbox = await screen.findByRole('checkbox', {
      name: 'instances.logfile.wrap_lines',
    });
    await waitFor(() => {
      expect(checkbox).not.toBeChecked();
    });
  });
});
