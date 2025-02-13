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
        },
      };

      render(HealthDetails, {
        props: {
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
