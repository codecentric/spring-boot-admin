/*
 * Copyright 2014-2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { nextTick, ref } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { useInstanceData } from '@/composables/useInstanceData';

vi.mock('@/composables/useApplicationStore', () => ({
  useApplicationStore: vi.fn(),
}));

const makeInstance = (id: string, metadata: Record<string, string>) => ({
  id,
  info: {},
  metadata,
  registration: { metadata },
});

const makeApplication = (instance: ReturnType<typeof makeInstance>) => ({
  instances: [instance],
  findInstance: (id: string) => (instance.id === id ? instance : undefined),
});

describe('useInstanceData', () => {
  let applications: ReturnType<typeof ref>;

  beforeEach(() => {
    applications = ref([]);

    // @ts-ignore
    useApplicationStore.mockReturnValue({ applications });
  });

  it('should update metadata reactively when the store replaces the instance', async () => {
    // Given
    const instanceV1 = makeInstance('abc', { env: 'prod' });
    applications.value = [makeApplication(instanceV1)];
    const { metadata } = useInstanceData('abc');

    // When — SSE update: store replaces the instance object entirely (same id, new metadata)
    const instanceV2 = makeInstance('abc', { env: 'staging' });
    applications.value = [makeApplication(instanceV2)];
    await nextTick();

    // Then
    expect(metadata.value).toEqual({ env: 'staging' });
  });

  it('should reflect initial metadata before any store update', () => {
    // Given
    const instance = makeInstance('abc', { env: 'prod' });
    applications.value = [makeApplication(instance)];

    // When
    const { metadata } = useInstanceData('abc');

    // Then
    expect(metadata.value).toEqual({ env: 'prod' });
  });

  it('should not update metadata when new instance metadata is deeply equal', async () => {
    // Given
    const instanceV1 = makeInstance('abc', { env: 'prod' });
    applications.value = [makeApplication(instanceV1)];
    const { metadata } = useInstanceData('abc');
    const referenceBeforeUpdate = metadata.value;

    // When — store replaces the instance object but metadata content is identical
    const instanceV2 = makeInstance('abc', { env: 'prod' });
    applications.value = [makeApplication(instanceV2)];
    await nextTick();

    // Then — shallowRef value must not have been replaced (identity preserved)
    expect(metadata.value).toBe(referenceBeforeUpdate);
  });

  describe('MaybeRefOrGetter instanceId', () => {
    it('should resolve instance data when instanceId is a Ref and switch instances when the Ref value changes', async () => {
      // Given
      const instanceA = makeInstance('aaa', { env: 'prod' });
      const instanceB = makeInstance('bbb', { env: 'staging' });
      applications.value = [
        makeApplication(instanceA),
        makeApplication(instanceB),
      ];
      const instanceId = ref('aaa');
      const { metadata } = useInstanceData(instanceId);
      expect(metadata.value).toEqual({ env: 'prod' });

      // When
      instanceId.value = 'bbb';
      await nextTick();

      // Then
      expect(metadata.value).toEqual({ env: 'staging' });
    });

    it('should resolve instance data when instanceId is a getter and switch instances when the getter return value changes', async () => {
      // Given
      const instanceA = makeInstance('aaa', { env: 'prod' });
      const instanceB = makeInstance('bbb', { env: 'staging' });
      applications.value = [
        makeApplication(instanceA),
        makeApplication(instanceB),
      ];
      const activeId = ref('aaa');
      const { metadata } = useInstanceData(() => activeId.value);
      expect(metadata.value).toEqual({ env: 'prod' });

      // When
      activeId.value = 'bbb';
      await nextTick();

      // Then
      expect(metadata.value).toEqual({ env: 'staging' });
    });

    it('should return undefined metadata when instanceId points to an unknown instance', () => {
      // Given
      applications.value = [
        makeApplication(makeInstance('known', { env: 'prod' })),
      ];

      // When
      const { metadata, instance } = useInstanceData('unknown-id');

      // Then
      expect(instance.value).toBeUndefined();
      expect(metadata.value).toBeUndefined();
    });
  });
});
