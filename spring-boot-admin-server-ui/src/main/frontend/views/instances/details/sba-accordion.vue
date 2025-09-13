<!--
  - Copyright 2014-2025 the original author or authors.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <sba-panel
    v-bind="$attrs"
    :aria-expanded="_open"
    :class-for-default-slot="{
      'transition-[height] h-auto': true,
      '!p-0 !h-0 overflow-hidden': !_open,
    }"
    @title-click="handleTitleClick"
  >
    <template #title>
      <slot name="title" />
    </template>

    <template #prefix>
      <font-awesome-icon
        icon="chevron-down"
        :class="{
          '-rotate-90': !_open,
          'mr-2 transition-[transform]': true,
        }"
      />
    </template>
    <template #actions>
      <slot name="actions" />
    </template>

    <template #default>
      <slot />
    </template>
  </sba-panel>
</template>

<script setup lang="ts">
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome';
import { onMounted, ref } from 'vue';

const { id = null } = defineProps<{
  id?: string;
}>();

const open = defineModel({ default: true, type: Boolean });
const emit = defineEmits<{
  (e: 'update:modelValue', payload: boolean): void;
}>();

const _open = ref(open.value);

onMounted(() => {
  if (id) {
    const storedValue = localStorage.getItem(
      `de.codecentric.spring-boot-admin.accordion.${id}.open`,
    );
    if (storedValue !== null) {
      _open.value = storedValue === 'true';
      emit('update:modelValue', !_open.value);
    }
  }
});

const handleTitleClick = () => {
  _open.value = !_open.value;
  emit('update:modelValue', !_open.value);

  if (id) {
    localStorage.setItem(
      `de.codecentric.spring-boot-admin.accordion.${id}.open`,
      _open.value.toString(),
    );
  }
};
</script>
