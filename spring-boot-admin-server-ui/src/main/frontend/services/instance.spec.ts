import { AxiosError } from 'axios';
import { describe, expect, test, vi } from 'vitest';

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
      registration: { name: 'test' },
      availableMetrics: ['test.metric', 'cache.size', 'cache.gets'],
    });
    test('should pass suppressToast option to axios config', async () => {
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
});
