import { screen } from '@testing-library/vue';
import { deepMerge } from '@vitest/utils';

import Instance from '@/services/instance';
import { render } from '@/test-utils';
import InstancesList from '@/views/applications/InstancesList.vue';

describe('InstancesList', () => {
  describe('Metadata: hide-url', () => {
    it('should show service url when hide-url is set to false', async () => {
      render(InstancesList, {
        props: {
          instances: [
            createInstance({
              registration: {
                metadata: { 'hide-url': 'false' },
              },
            }),
          ],
        },
      });

      expect(await screen.queryByText('Spring Boot Admin')).toBeVisible();
      expect(await screen.queryByText('http://localhost:8080')).toBeVisible();
    });

    it('should show service url when hide-url not set', async () => {
      render(InstancesList, {
        props: {
          instances: [createInstance()],
        },
      });

      expect(await screen.queryByText('Spring Boot Admin')).toBeVisible();
      expect(await screen.queryByText('http://localhost:8080')).toBeVisible();
    });

    it('should hide service url when hide-url is set to true', async () => {
      render(InstancesList, {
        props: {
          instances: [
            createInstance({
              registration: {
                metadata: { 'hide-url': 'true' },
              },
            }),
          ],
        },
      });

      expect(await screen.queryByText('Spring Boot Admin')).toBeVisible();
      expect(
        await screen.queryByText('http://localhost:8080'),
      ).not.toBeInTheDocument();
    });
  });

  describe('Metadata: disable-url', () => {
    it('should show service url homepage button when diable-url is set to false', async () => {
      render(InstancesList, {
        props: {
          instances: [
            createInstance({
              registration: {
                metadata: { 'disable-url': 'false' },
              },
            }),
          ],
        },
      });

      expect(
        await screen.queryByRole('link', { name: 'Homepage' }),
      ).toBeVisible();
    });

    it('should show service url homepage button when diable-url is not set', async () => {
      render(InstancesList, {
        props: {
          instances: [createInstance()],
        },
      });

      expect(
        await screen.queryByRole('link', { name: 'Homepage' }),
      ).toBeVisible();
    });

    it('should show service url homepage button when diable-url is set to true', async () => {
      render(InstancesList, {
        props: {
          instances: [
            createInstance({
              registration: {
                metadata: { 'disable-url': 'true' },
              },
            }),
          ],
        },
      });

      expect(
        await screen.queryByRole('link', { name: 'Homepage' }),
      ).not.toBeInTheDocument();
    });
  });
});

// Utility functions

function createInstance(options: InstanceType<typeof Instance> = {}): Instance {
  const defaultData = {
    id: 'Spring Boot Admin',
    statusInfo: { status: 'UP' },
    registration: {
      serviceUrl: 'http://localhost:8080',
    },
  };

  const instance = deepMerge(defaultData, options);
  return new Instance(instance);
}
