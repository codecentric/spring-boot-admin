import { screen } from '@testing-library/vue';
import { describe, expect, it } from 'vitest';

import { applications } from '@/mocks/applications/data';
import Application from '@/services/application';
import { render } from '@/test-utils';
import Dependencies from '@/views/instances/dependencies/index.vue';

describe('Dependencies', () => {
  describe('Render correctly', () => {
    it('should render both sbom ids', async () => {
      const application = new Application(applications[0]);
      const instance = application.instances[0];

      render(Dependencies, {
        props: {
          instance,
        },
      });

      expect(
        await screen.findByText(/application \(63\/63\)/),
      ).toBeVisible();

      expect(
        await screen.findByText(/system \(63\/63\)/),
      ).toBeVisible();
    });
  });
});
