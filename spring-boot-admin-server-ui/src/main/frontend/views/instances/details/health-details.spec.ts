import { screen, within } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import { render } from '@/test-utils';
import HealthDetails from '@/views/instances/details/health-details.vue';

describe('HealthDetails', () => {
  describe('Health .details', () => {
    beforeEach(() => {
      const healthMock = {
        status: 'UP',
        details: {
          clientConfigServer: {
            status: 'UNKNOWN',
            details: { error: 'no property sources located' },
          },
          db: {
            status: 'UP',
            details: {
              database: 'HSQL Database Engine',
              validationQuery: 'isValid()',
            },
          },
          discoveryComposite: {
            description: 'Discovery Client not initialized',
            status: 'UNKNOWN',
            details: {
              discoveryClient: {
                description: 'Discovery Client not initialized',
                status: 'DOWN',
              },
            },
          },
          diskSpace: {
            status: 'UP',
            details: {
              total: 994662584320,
              free: 300063879168,
              threshold: 10485760,
              exists: true,
            },
          },
          diskSpace2: {
            status: 'UP',
            details: {
              total: 1024,
              free: 2048,
              threshold: 4096,
              exists: false,
            },
          },
          ssl: {
            status: 'UP',
            details: {
              validChains: JSON.parse(
                '[{"status": "VALID", "chain": [{"subject": "CN=example.com, OU=IT, O=Example Corp, L=San Francisco, ST=CA, C=US", "issuer": "CN=R3, O=Let\'s Encrypt, C=US"}]}]',
              ),
            },
          },
        },
      };

      render(HealthDetails, {
        props: {
          name: 'Name',
          health: healthMock,
        },
      });
    });

    it.each`
      componentId             | status
      ${'clientConfigServer'} | ${'UNKNOWN'}
      ${'discoveryComposite'} | ${'UNKNOWN'}
      ${'discoveryClient'}    | ${'DOWN'}
      ${'diskSpace'}          | ${'UP'}
    `(
      'should display health components status',
      async ({ componentId, status }) => {
        const clientConfigServer = await screen.findByRole('definition', {
          name: componentId,
        });
        expect(
          await within(clientConfigServer).findByRole('status'),
        ).toHaveTextContent(status);
      },
    );

    it('should format diskSpace details correctly', async () => {
      const diskSpaceInfo = await screen.findByRole('definition', {
        name: 'diskSpace2',
      });

      // Assert pretty-printed numbers via pretty-bytes and other primitive values
      const dsi = within(diskSpaceInfo);
      // total: 994662584320 bytes -> 995 GB (rounded)
      expect(
        await dsi.findByRole('definition', { name: 'total' }),
      ).toHaveTextContent('1.02 kB');

      // free: 300063879168 bytes -> 300 GB (rounded)
      expect(
        await dsi.findByRole('definition', { name: 'free' }),
      ).toHaveTextContent('2.05 kB');

      // threshold: 10485760 bytes -> 10 MB
      expect(
        await dsi.findByRole('definition', { name: 'threshold' }),
      ).toHaveTextContent('4.1 kB');

      // exists: boolean unchanged
      expect(
        await dsi.findByRole('definition', { name: 'exists' }),
      ).toHaveTextContent('false');
    });

    it('should format object details correctly', async () => {
      const sslInfo = await screen.findByRole('definition', {
        name: 'validChains',
      });
      expect(sslInfo).toMatchSnapshot();
    });
  });

  describe('Health .components', () => {
    beforeEach(() => {
      const healthMock = {
        status: 'UP',
        components: {
          clientConfigServer: {
            status: 'UNKNOWN',
            details: { error: 'no property sources located' },
          },
          discoveryComposite: {
            description: 'Discovery Client not initialized',
            status: 'UNKNOWN',
            components: {
              discoveryClient: {
                description: 'Discovery Client not initialized',
                status: 'DOWN',
              },
            },
          },
          diskSpace: {
            status: 'UP',
            details: {
              total: 994662584320,
              free: 116363821056,
              threshold: 10485760,
              exists: true,
            },
          },
        },
      };

      render(HealthDetails, {
        props: {
          health: healthMock,
        },
      });
    });

    it('should display health status', async () => {
      expect(
        screen.getByRole('definition', { name: 'clientConfigServer' }),
      ).toBeInTheDocument();
    });

    it.each`
      componentId             | status
      ${'clientConfigServer'} | ${'UNKNOWN'}
      ${'discoveryComposite'} | ${'UNKNOWN'}
      ${'discoveryClient'}    | ${'DOWN'}
      ${'diskSpace'}          | ${'UP'}
    `(
      'should display health components status',
      async ({ componentId, status }) => {
        const clientConfigServer = await screen.findByRole('definition', {
          name: componentId,
        });
        expect(
          await within(clientConfigServer).findByRole('status'),
        ).toHaveTextContent(status);
      },
    );
  });
});
