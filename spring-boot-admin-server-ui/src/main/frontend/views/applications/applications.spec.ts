import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { HttpResponse, http } from 'msw';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { Ref, ref } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import Instance, { Registration } from '@/services/instance';
import NotificationFilter from '@/services/notification-filter';
import { render } from '@/test-utils';
import Applications from '@/views/applications/index.vue';

vi.mock('@/composables/useApplicationStore', () => ({
  useApplicationStore: vi.fn(),
}));

describe('Applications', () => {
  let applicationsInitialized: Ref<boolean>;
  let applications: Ref<Application[]>;
  let error: Ref<any>;

  beforeEach(async () => {
    applicationsInitialized = ref(false);
    applications = ref([]);
    error = ref(null);

    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    useApplicationStore.mockReturnValue({
      applicationStore: {
        findApplicationByInstanceId: (id: string) => {
          return applications.value.find((a) => {
            return a.instances.some((i) => i.id === id);
          });
        },
      },
      applicationsInitialized,
      applications,
      error,
    });

    render(Applications);
  });

  it('when applications are loading, a hint should be shown', async () => {
    expect(await screen.findByText('Loading applications...')).toBeVisible();
  });

  it('when there are no applications, a corresponding text is shown', async () => {
    applicationsInitialized.value = true;

    await waitFor(() => {
      expect(screen.getByText('No applications registered.')).toBeVisible();
    });
  });

  describe('when there are applications', () => {
    beforeEach(async () => {
      const instance = new Instance({
        id: 'id',
        statusInfo: {
          status: 'UP',
        },
        registration: {
          name: 'spring-boot-admin-sample-servlet',
          serviceUrl: 'serviceUrl',
          metadata: {},
        } as Registration,
      });

      applicationsInitialized.value = true;
      applications.value = [
        new Application({
          id: 'app-id',
          name: 'spring-boot-admin-sample-servlet',
          instances: [instance],
          status: 'UP',
        }),
      ];
    });

    it('name of applications are shown', async () => {
      await waitFor(() => {
        expect(
          screen.getByRole('button', {
            name: /spring-boot-admin-sample-servlet/i,
          }),
        ).toBeVisible();
      });
    });

    it('when the search does not match, a corresponding text is shown', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'does-not-match');

      expect(
        await screen.findByText('No results matching your filter.'),
      ).toBeVisible();
    });

    it('when the search matches, the application is shown', async () => {
      const filterInput = screen.getByLabelText('Filter');
      await userEvent.type(filterInput, 'sample');

      await waitFor(() => {
        expect(
          screen.getByRole('button', {
            name: /spring-boot-admin-sample-servlet/i,
          }),
        ).toBeVisible();
      });
    });

    it('clicking on the name opens list of instances', async () => {
      const applicationElement = await screen.findByRole('button', {
        name: /spring-boot-admin-sample-servlet/i,
      });

      await userEvent.click(applicationElement);

      expect(await screen.findByText('serviceUrl')).toBeVisible();
    });

    describe('application list', () => {
      beforeEach(async () => {
        applications.value = [
          new Application({
            id: 'app-id',
            name: 'spring-boot-admin-sample-servlet-up',
            instances: [
              {
                id: 'id',
                statusInfo: {
                  status: 'UP',
                },
                registration: {
                  name: 'spring-boot-admin-sample-servlet-up',
                  serviceUrl: 'serviceUrl',
                  metadata: {},
                } as Registration,
              },
            ],
            status: 'UP',
          }),
          new Application({
            id: 'app-id2',
            name: 'spring-boot-admin-sample-servlet-down',
            instances: [
              {
                id: 'id2',
                statusInfo: {
                  status: 'DOWN',
                },
                registration: {
                  name: 'spring-boot-admin-sample-servlet-down',
                  serviceUrl: 'serviceUrl',
                  metadata: {},
                } as Registration,
              },
            ],
            status: 'DOWN',
          }),
          new Application({
            id: 'app-id3',
            name: 'spring-boot-admin-sample-servlet-up2',
            instances: [
              {
                id: 'id5',
                statusInfo: {
                  status: 'UP',
                },
                registration: {
                  name: 'spring-boot-admin-sample-servlet-up2',
                  serviceUrl: 'serviceUrl',
                  metadata: {},
                } as Registration,
              },
            ],
            status: 'UP',
          }),
          new Application({
            id: 'app-id4',
            name: 'spring-boot-admin-sample-servlet-restricted',
            instances: [
              {
                id: 'id6',
                statusInfo: {
                  status: 'UP',
                },
                registration: {
                  name: 'spring-boot-admin-sample-servlet-restricted',
                  serviceUrl: 'serviceUrl',
                  metadata: {},
                } as Registration,
              },
              {
                id: 'id7',
                statusInfo: {
                  status: 'DOWN',
                },
                registration: {
                  name: 'spring-boot-admin-sample-servlet-restricted',
                  serviceUrl: 'serviceUrl',
                  metadata: {},
                } as Registration,
              },
            ],
            status: 'RESTRICTED',
          }),
        ];
      });

      it('should show applications ordered by status DOWN, RESTRICTED and UP', () => {
        const allByRole = screen.getAllByRole('button');

        const getIndex = (status: string) =>
          allByRole.findIndex((element: HTMLElement) =>
            element.textContent.startsWith(
              `${status}spring-boot-admin-sample-servlet`,
            ),
          );

        const indexDown = getIndex('DOWN');
        const indexRestricted = getIndex('RESTRICTED');
        const indexUp = getIndex('UP');

        expect(indexDown).toBeGreaterThan(-1);
        expect(indexDown).toBeLessThan(indexRestricted);
        expect(indexRestricted).toBeLessThan(indexUp);
      });
    });

    describe('refresh button', () => {
      beforeEach(() => {
        server.use(
          http.post('/applications', () => {
            return HttpResponse.json({});
          }),
        );
      });

      it('clicking the refresh button invokes Application.refreshApplications', async () => {
        const refreshSpy = vi.spyOn(Application, 'refreshApplications');

        const refreshButton = screen.getByTitle('Refresh applications');
        // First click - enters confirm mode
        await userEvent.click(refreshButton);

        // Second click - confirms and executes
        const confirmButton = await screen.findByText('Confirm');
        await userEvent.click(confirmButton);

        await waitFor(() => {
          expect(refreshSpy).toHaveBeenCalled();
        });
      });

      it('logs error when refresh fails without throwing', async () => {
        server.use(
          http.post('/applications', () => {
            return HttpResponse.json({}, { status: 500 });
          }),
        );

        const consoleErrorSpy = vi
          .spyOn(console, 'error')
          .mockImplementation(() => {});

        const refreshButton = screen.getByTitle('Refresh applications');
        // First click - enters confirm mode
        await userEvent.click(refreshButton);

        // Second click - confirms and executes
        const confirmButton = await screen.findByText('Confirm');
        await userEvent.click(confirmButton);

        await waitFor(() => {
          expect(consoleErrorSpy).toHaveBeenCalled();
        });

        consoleErrorSpy.mockRestore();
      });
    });
  });
});

describe('Applications add notification filter', () => {
  const renderWithApplication = (application: Application) => {
    // eslint-disable-next-line @typescript-eslint/ban-ts-comment
    // @ts-ignore
    useApplicationStore.mockReturnValue({
      applicationStore: {
        findApplicationByInstanceId: (id: string) =>
          application.instances.find((i) => i.id === id) ? application : null,
      },
      applicationsInitialized: ref(true),
      applications: ref([application]),
      error: ref(null),
    });

    render(Applications);
  };

  const createApplication = (name: string, instanceId: string) =>
    new Application({
      id: 'app-id',
      name,
      status: 'UP',
      instances: [
        new Instance({
          id: instanceId,
          statusInfo: { status: 'UP' },
          registration: {
            name,
            serviceUrl: 'serviceUrl',
            metadata: {},
          } as Registration,
        }),
      ],
    });

  const clearToasts = () =>
    // The toasts are rendered outside of the component tree, hence they are
    // not removed by the testing-library cleanup.
    document
      .querySelectorAll('.v-toast-container')
      .forEach((container) => container.remove());

  const getToast = async () =>
    await waitFor(() => {
      const toast = document.querySelector('.v-toast__message');
      expect(toast).not.toBeNull();
      return toast as HTMLElement;
    });

  beforeEach(() => {
    clearToasts();
    vi.spyOn(NotificationFilter, 'isSupported').mockReturnValue(true);
    vi.spyOn(NotificationFilter, 'getFilters').mockResolvedValue({
      data: [],
    } as any);
  });

  afterEach(() => {
    clearToasts();
    delete (window as any).__xss;
  });

  it('strips dangerous markup contained in the application name from the success toast', async () => {
    const maliciousName = '<img src=x onerror="window.__xss=1">rogue-app';
    vi.spyOn(NotificationFilter, 'addFilter').mockResolvedValue({
      data: new NotificationFilter({
        id: 'filter-id',
        applicationName: maliciousName,
        expiry: Date.now() + 5 * 60 * 1000,
      }),
    } as any);

    renderWithApplication(createApplication(maliciousName, 'instance-id'));

    await userEvent.click(await screen.findByTitle('Notification filters'));
    await userEvent.click(await screen.findByText('Suppress'));

    const toast = await getToast();

    // The application name still shows up as text ...
    expect(toast.textContent).toContain('rogue-app');
    // ... and so does the expiry, which is intentionally rendered as markup ...
    expect(toast.querySelector('strong')).toHaveTextContent('5 minutes');
    // ... but the <img onerror=...> must have been stripped by sanitizeHtml,
    // it must never be parsed into a real element / fire its handler.
    expect(toast.querySelector('img')).toBeNull();
    expect((window as any).__xss).toBeUndefined();
  });

  it('strips script tags contained in the instance id from the success toast', async () => {
    const maliciousId = '<script>window.__xss=1</script>evil-instance';
    vi.spyOn(NotificationFilter, 'addFilter').mockResolvedValue({
      data: new NotificationFilter({
        id: 'filter-id',
        instanceId: maliciousId,
        expiry: Date.now() + 5 * 60 * 1000,
      }),
    } as any);

    renderWithApplication(createApplication('an-application', maliciousId));

    // Expand the application to get hold of the instance level actions.
    await userEvent.click(
      await screen.findByRole('button', { name: /an-application/i }),
    );
    await userEvent.click(
      await waitFor(() => {
        const button = document.getElementById(`nf-settings-${maliciousId}`);
        expect(button).not.toBeNull();
        return button as HTMLElement;
      }),
    );
    await userEvent.click(await screen.findByText('Suppress'));

    const toast = await getToast();

    expect(toast.textContent).toContain('evil-instance');
    expect(toast.querySelector('script')).toBeNull();
    expect((window as any).__xss).toBeUndefined();
  });
});
