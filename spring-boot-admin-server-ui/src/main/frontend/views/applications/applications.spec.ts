import userEvent from '@testing-library/user-event';
import { screen, waitFor } from '@testing-library/vue';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Ref, ref } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import Application from '@/services/application';
import Instance, { Registration } from '@/services/instance';
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
  });
});
