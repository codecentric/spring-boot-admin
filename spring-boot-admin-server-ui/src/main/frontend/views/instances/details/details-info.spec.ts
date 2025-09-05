import { screen, waitFor } from '@testing-library/vue';
import { AxiosHeaders } from 'axios';
import { HttpResponse, http } from 'msw';
import { beforeEach, describe, expect, it, vi } from 'vitest';

import DetailsInfo from './details-info.vue';

import { applications } from '@/mocks/applications/data';
import { server } from '@/mocks/server';
import Application from '@/services/application';
import { render } from '@/test-utils';

describe('DetailsInfo', () => {
  beforeEach(() => {
    server.use(
      http.get('/instances/:instanceId/actuator/info', () => {
        return HttpResponse.json({
          app: { version: '1.0.0', name: 'TestApp' },
          java: { version: '17' },
        });
      }),
    );
  });

  it('should render info table with keys and values', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];
    instance.hasEndpoint = () => true;
    // Use AxiosResponse type for the mock
    // eslint-disable-next-line @typescript-eslint/no-var-requires

    instance.fetchInfo = async () => ({
      data: {
        app: { version: '1.0.0', name: 'TestApp' },
        java: { version: '17' },
      },
      status: 200,
      statusText: 'OK',
      headers: new AxiosHeaders(),
      config: { headers: new AxiosHeaders() },
    });

    render(DetailsInfo, {
      props: { instance },
    });

    await waitFor(() => {
      expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });

    expect(await screen.findByText('app')).toBeVisible();
    expect(await screen.findByText('java')).toBeVisible();
    // YAML-formatted output is rendered in a <pre> block, so match the full string
    expect(
      await screen.findByText(/version: 1.0.0[\s\S]*name: TestApp/),
    ).toBeVisible();
    expect(await screen.findByText(/version: '17'/)).toBeVisible();
  });

  it('should show no info message if info is empty', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];
    instance.hasEndpoint = () => true;
    instance.fetchInfo = async () => ({
      data: {},
      status: 200,
      statusText: 'OK',
      headers: new AxiosHeaders(),
      config: { headers: new AxiosHeaders() },
    });

    render(DetailsInfo, {
      props: { instance },
    });

    await waitFor(() => {
      expect(screen.queryByRole('status')).not.toBeInTheDocument();
    });

    expect(
      await screen.findByText('instances.details.info.no_info_provided'),
    ).toBeVisible();
  });

  it('should show error alert if fetch fails', async () => {
    const application = new Application(applications[0]);
    const instance = application.instances[0];
    instance.hasEndpoint = () => true;
    instance.fetchInfo = async () => {
      throw new Error('fail');
    };

    render(DetailsInfo, {
      props: { instance },
    });

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeVisible();
    });
    expect(screen.getByRole('alert')).toHaveTextContent(
      'Fetching of data failed.',
    );
  });

  it('should call fetchInfo when instance changes (watcher)', async () => {
    const application = new Application(applications[0]);
    const instance1 = application.instances[0];
    const instance2 = {
      ...instance1,
      id: 'other-id',
      hasEndpoint: () => true,
      fetchInfo: async () => ({ data: { foo: 'bar' } }),
    };
    instance1.hasEndpoint = () => true;
    const fetchInfoSpy = vi
      .fn()
      .mockResolvedValue({ data: { app: { version: '1.0.0' } } });
    instance1.fetchInfo = fetchInfoSpy;

    const { rerender } = render(DetailsInfo, {
      props: { instance: instance1 },
    });

    await waitFor(() => {
      expect(fetchInfoSpy).toHaveBeenCalled();
    });

    // Now rerender with a new instance (different id)
    await rerender({ instance: instance2 });

    // Should show the new info from instance2
    expect(await screen.findByText('foo')).toBeVisible();
    expect(await screen.findByText('bar')).toBeVisible();
  });
});
