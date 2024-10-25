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

      expect(instance.showUrl()).toBe(expectUrlToBeShownOnUI);
    },
  );
});
