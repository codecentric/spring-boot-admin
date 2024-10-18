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
    showInstanceUrl | metadataHideUrl | expected
    ${true}         | ${'true'}       | ${true}
    ${false}        | ${'true'}       | ${false}
    ${true}         | ${'false'}      | ${false}
    ${false}        | ${'false'}      | ${false}
    ${true}         | ${undefined}    | ${true}
  `(
    'showUrl when showInstanceUrl=$showInstanceUrl and metadataHideUrl=$metadataHideUrl should return $expected',
    ({ showInstanceUrl, metadataHideUrl, expected }) => {
      useSbaConfig.mockReturnValue({
        uiSettings: {
          showInstanceUrl: showInstanceUrl,
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

      expect(instance.showUrl()).toBe(expected);
    },
  );
});
