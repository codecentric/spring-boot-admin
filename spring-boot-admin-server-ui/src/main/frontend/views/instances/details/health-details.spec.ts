import userEvent from '@testing-library/user-event';
import { screen, within } from '@testing-library/vue';
import { beforeEach, describe, expect, it } from 'vitest';

import Instance from '@/services/instance';
import { render } from '@/test-utils';
import HealthDetails from '@/views/instances/details/health-details.vue';

describe('HealthDetails', () => {
  const mockInstance = {
    id: 'test-instance-123',
  } as Instance;

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
          instance: mockInstance,
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
          name: 'root',
          instance: mockInstance,
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

  describe('Collapsible details functionality', () => {
    beforeEach(() => {
      // Clear localStorage before each test
      localStorage.clear();
    });

    it('should show toggle button when details exist', async () => {
      const healthMock = {
        status: 'UP',
        details: {
          database: 'HSQL Database Engine',
          validationQuery: 'isValid()',
        },
      };

      render(HealthDetails, {
        props: {
          name: 'db',
          health: healthMock,
          instance: mockInstance,
        },
      });

      const toggleButton = await screen.findByRole('button');
      expect(toggleButton).toBeInTheDocument();
      expect(toggleButton).toHaveAttribute('title', 'Toggle db health details');
    });

    it('should have proper ARIA attributes on toggle button', async () => {
      const healthMock = {
        status: 'UP',
        details: {
          database: 'HSQL Database Engine',
        },
      };

      render(HealthDetails, {
        props: {
          name: 'db',
          health: healthMock,
          instance: mockInstance,
        },
      });

      const toggleButton = await screen.findByRole('button');

      // Should have title with translated text
      expect(toggleButton).toHaveAttribute('title', 'Toggle db health details');

      // Should have aria-expanded set to false initially (collapsed)
      expect(toggleButton).toHaveAttribute('aria-expanded', 'false');

      // Should have aria-controls pointing to the details element
      expect(toggleButton).toHaveAttribute('aria-controls');
      const controlsId = toggleButton.getAttribute('aria-controls');
      expect(controlsId).toContain('health-details');
      expect(controlsId).toContain('db');
    });

    it('should update aria-expanded when toggled', async () => {
      const user = userEvent.setup();
      const healthMock = {
        status: 'UP',
        details: {
          database: 'HSQL Database Engine',
        },
      };

      render(HealthDetails, {
        props: {
          name: 'db',
          health: healthMock,
          instance: mockInstance,
        },
      });

      const toggleButton = await screen.findByRole('button');

      // Initially collapsed
      expect(toggleButton).toHaveAttribute('aria-expanded', 'false');

      // After clicking - expanded
      await user.click(toggleButton);
      expect(toggleButton).toHaveAttribute('aria-expanded', 'true');

      // After clicking again - collapsed
      await user.click(toggleButton);
      expect(toggleButton).toHaveAttribute('aria-expanded', 'false');
    });

    it('should not show toggle button when no details exist', async () => {
      const healthMock = {
        status: 'UP',
      };

      render(HealthDetails, {
        props: {
          name: 'simple',
          health: healthMock,
          instance: mockInstance,
        },
      });

      const toggleButton = screen.queryByRole('button');
      expect(toggleButton).not.toBeInTheDocument();
    });

    it('should start collapsed by default', async () => {
      const healthMock = {
        status: 'UP',
        details: {
          database: 'HSQL Database Engine',
          validationQuery: 'isValid()',
        },
      };

      render(HealthDetails, {
        props: {
          name: 'db',
          health: healthMock,
          instance: mockInstance,
        },
      });

      // Details should not be visible initially
      expect(
        screen.queryByText('HSQL Database Engine'),
      ).not.toBeInTheDocument();
    });

    it('should expand details when toggle button is clicked', async () => {
      const user = userEvent.setup();
      const healthMock = {
        status: 'UP',
        details: {
          database: 'HSQL Database Engine',
          validationQuery: 'isValid()',
        },
      };

      render(HealthDetails, {
        props: {
          name: 'db',
          health: healthMock,
          instance: mockInstance,
        },
      });

      const toggleButton = await screen.findByRole('button');
      await user.click(toggleButton);

      // Details should now be visible
      expect(
        await screen.findByText('HSQL Database Engine'),
      ).toBeInTheDocument();
      expect(screen.getByText('isValid()')).toBeInTheDocument();
    });

    it('should collapse details when toggle button is clicked twice', async () => {
      const user = userEvent.setup();
      const healthMock = {
        status: 'UP',
        details: {
          database: 'HSQL Database Engine',
        },
      };

      render(HealthDetails, {
        props: {
          name: 'db',
          health: healthMock,
          instance: mockInstance,
        },
      });

      const toggleButton = await screen.findByRole('button');

      // First click - expand
      await user.click(toggleButton);
      expect(
        await screen.findByText('HSQL Database Engine'),
      ).toBeInTheDocument();

      // Second click - collapse
      await user.click(toggleButton);
      expect(
        screen.queryByText('HSQL Database Engine'),
      ).not.toBeInTheDocument();
    });

    it('should persist collapsed state in localStorage', async () => {
      const user = userEvent.setup();
      const healthMock = {
        status: 'UP',
        details: {
          database: 'HSQL Database Engine',
        },
      };

      render(HealthDetails, {
        props: {
          name: 'db',
          health: healthMock,
          instance: mockInstance,
        },
      });

      const toggleButton = await screen.findByRole('button');
      await user.click(toggleButton);

      const storageKey = `de.codecentric.spring-boot-admin.health-details.db.${mockInstance.id}.collapsed`;
      expect(localStorage.getItem(storageKey)).toBe('false');
    });

    it('should restore collapsed state from localStorage', async () => {
      const healthMock = {
        status: 'UP',
        details: {
          database: 'HSQL Database Engine',
        },
      };

      const storageKey = `de.codecentric.spring-boot-admin.health-details.db.${mockInstance.id}.collapsed`;
      localStorage.setItem(storageKey, 'false');

      render(HealthDetails, {
        props: {
          name: 'db',
          health: healthMock,
          instance: mockInstance,
        },
      });

      // Details should be visible because we set collapsed to false in localStorage
      expect(
        await screen.findByText('HSQL Database Engine'),
      ).toBeInTheDocument();
    });

    it('should handle child health components correctly', async () => {
      const user = userEvent.setup();
      const healthMock = {
        status: 'UP',
        components: {
          db: {
            status: 'UP',
            details: {
              database: 'HSQL Database Engine',
            },
          },
        },
      };

      render(HealthDetails, {
        props: {
          name: 'parent',
          health: healthMock,
          instance: mockInstance,
        },
      });

      // Child component should be rendered
      const dbComponent = await screen.findByRole('definition', { name: 'db' });
      expect(dbComponent).toBeInTheDocument();
    });

    it('should not show toggle button when details only contain child health components', async () => {
      const healthMock = {
        status: 'UP',
        details: {
          childComponent: {
            status: 'UP',
            details: {},
          },
        },
      };

      render(HealthDetails, {
        props: {
          name: 'parent',
          health: healthMock,
          instance: mockInstance,
        },
      });

      // No toggle button because all details are child health components
      const toggleButton = screen.queryByRole('button');
      expect(toggleButton).not.toBeInTheDocument();
    });
  });
});
