import { AxiosError } from 'axios';
import { http } from 'msw';
import { firstValueFrom } from 'rxjs';
import { describe, expect, it, test, vi } from 'vitest';

import { server } from '@/mocks/server';
import Instance from '@/services/instance';

const { useSbaConfig } = vi.hoisted(() => ({
  useSbaConfig: vi.fn().mockReturnValue(true),
}));

vi.mock('@/sba-config', async (importOriginal) => ({
  ...(await importOriginal<typeof import('@/sba-config')>()),
  useSbaConfig,
}));

describe('Instance', () => {
  test.each`
    hideInstanceUrl | metadataHideUrl | expectUrlToBeShownOnUI
    ${false}        | ${'true'}       | ${false}
    ${false}        | ${'false'}      | ${true}
    ${false}        | ${undefined}    | ${true}
    ${true}         | ${'true'}       | ${false}
    ${true}         | ${'false'}      | ${false}
  `(
    'showUrl when hideInstanceUrl=$hideInstanceUrl and metadataHideUrl=$metadataHideUrl should return $expectUrlToBeShownOnUI',
    ({ hideInstanceUrl, metadataHideUrl, expectUrlToBeShownOnUI }) => {
      useSbaConfig.mockReturnValue({
        uiSettings: {
          hideInstanceUrl,
        },
      });

      const instance = new Instance({
        id: 'id',
        // eslint-disable-next-line @typescript-eslint/ban-ts-comment
        // @ts-expect-error
        registration: {
          metadata: {
            ['hide-url']: metadataHideUrl,
          },
        },
      });

      expect(instance.showUrl()).toEqual(expectUrlToBeShownOnUI);
    },
  );

  describe('fetchMetric', () => {
    const instance = new Instance({
      id: 'test-id',
      registration: {
        name: 'test',
        healthUrl: '',
        source: '',
      },
      availableMetrics: ['test.metric', 'cache.size', 'cache.gets'],
    });
    test('should pass suppressToast option to axios config', async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      // Spy on axios.get
      const axiosGetSpy = vi.spyOn(instance.axios, 'get');

      // Mock the axios request
      axiosGetSpy.mockResolvedValue({
        data: {
          measurements: [{ value: 42 }],
        },
      });

      await instance.fetchMetric(
        'test.metric',
        { tag: 'value' },
        {
          suppressToast: true,
        },
      );

      // Verify suppressToast was passed in config
      expect(axiosGetSpy).toHaveBeenCalledWith(
        expect.stringContaining('actuator/metrics/test.metric'),
        expect.objectContaining({
          suppressToast: true,
        }),
      );
    });

    test('should work without options parameter for backward compatibility', async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      const axiosGetSpy = vi.spyOn(instance.axios, 'get');

      axiosGetSpy.mockResolvedValue({
        data: {
          measurements: [{ value: 42 }],
        },
      });

      await instance.fetchMetric('test.metric', { tag: 'value' });

      // Verify it was called without suppressToast
      expect(axiosGetSpy).toHaveBeenCalledWith(
        expect.stringContaining('actuator/metrics/test.metric'),
        expect.objectContaining({
          suppressToast: undefined,
        }),
      );
    });

    test('should pass suppressToast=false when explicitly set to false', async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      const axiosGetSpy = vi.spyOn(instance.axios, 'get');

      axiosGetSpy.mockResolvedValue({
        data: {
          measurements: [{ value: 42 }],
        },
      });

      await instance.fetchMetric(
        'test.metric',
        { tag: 'value' },
        {
          suppressToast: false,
        },
      );

      expect(axiosGetSpy).toHaveBeenCalledWith(
        expect.stringContaining('actuator/metrics/test.metric'),
        expect.objectContaining({
          suppressToast: false,
        }),
      );
    });

    test('should include tags in request parameters', async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      const axiosGetSpy = vi.spyOn(instance.axios, 'get');

      axiosGetSpy.mockResolvedValue({
        data: {
          measurements: [{ value: 42 }],
        },
      });

      await instance.fetchMetric('cache.gets', {
        name: 'my-cache',
        result: 'hit',
      });

      const callArgs = axiosGetSpy.mock.calls[0];
      const params = callArgs[1]?.params as URLSearchParams;

      expect(params).toBeInstanceOf(URLSearchParams);
      expect(params.getAll('tag')).toContain('name:my-cache');
      expect(params.getAll('tag')).toContain('result:hit');
    });

    test('should pass suppressToast function to axios config', async () => {
      // eslint-disable-next-line @typescript-eslint/ban-ts-comment
      // @ts-expect-error
      const axiosGetSpy = vi.spyOn(instance.axios, 'get');

      axiosGetSpy.mockResolvedValue({
        data: {
          measurements: [{ value: 42 }],
        },
      });

      const suppressFn = (err: AxiosError) => err.response?.status === 404;
      await instance.fetchMetric(
        'cache.size',
        {},
        { suppressToast: suppressFn },
      );

      expect(axiosGetSpy).toHaveBeenCalledWith(
        expect.any(String),
        expect.objectContaining({ suppressToast: suppressFn }),
      );
    });
  });

  describe('streamLogFile', () => {
    const instance = new Instance({
      id: 'test-id',
      registration: {
        name: 'test',
        healthUrl: '',
        source: '',
      },
    });

    it('should handle single JSON log line correctly', async () => {
      const payload = '{"foo":"bar"}';
      server.use(
        http.get(
          '/instances/:instanceId/actuator/logfile',
          () =>
            // As per Spring Boot definition: https://docs.spring.io/spring-boot/api/rest/actuator/logfile.html
            new Response(payload, {
              headers: {
                'Accept-Ranges': 'bytes',
                'Content-Type': 'text/plain;charset=UTF-8',
                'Content-Length': payload.length.toString(),
              },
            }),
        ),
      );

      const source = instance.streamLogfile(0);
      const { addendum } = await firstValueFrom(source);

      expect(addendum).toEqual(payload);
    });
  });
});
