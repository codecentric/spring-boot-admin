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
import { isEqual } from 'lodash-es';
import { MaybeRefOrGetter, computed, shallowRef, toValue, watch } from 'vue';

import { useApplicationStore } from '@/composables/useApplicationStore';
import { findApplicationForInstance, findInstance } from '@/store';

/**
 * Reads reactive instance and application data from the SSE-driven store.
 * Components using this composable receive live updates without holding a
 * reference to the Instance class — they only depend on the instanceId string.
 *
 * Accepts a plain string, a Ref<string>, or a getter () => string so that
 * components whose instanceId prop changes (e.g. via router navigation) stay
 * reactive without re-calling the composable.
 */
export function useInstanceData(instanceId: MaybeRefOrGetter<string>) {
  const { applications } = useApplicationStore();

  const instance = computed(() =>
    findInstance(applications.value, toValue(instanceId)),
  );

  const application = computed(() =>
    findApplicationForInstance(applications.value, toValue(instanceId)),
  );

  const info = shallowRef(instance.value?.info);
  const metadata = shallowRef(instance.value?.metadata);

  watch(
    instance,
    (newInstance) => {
      if (!isEqual(newInstance?.info, info.value)) {
        info.value = newInstance?.info;
      }
      if (!isEqual(newInstance?.metadata, metadata.value)) {
        metadata.value = newInstance?.metadata;
      }
    },
    { immediate: true },
  );

  return {
    instance,
    info,
    metadata,
    application,
  };
}
