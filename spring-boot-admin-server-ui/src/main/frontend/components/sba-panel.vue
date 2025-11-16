<!--
  - Copyright 2014-2019 the original author or authors.
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
  <div
    :id="$attrs.id"
    class="shadow-sm border rounded break-inside-avoid mb-4"
    :aria-expanded="$attrs.ariaExpanded"
  >
    <header
      v-if="hasTitle"
      ref="header"
      v-sticks-below="props.headerSticksBelow"
      class="rounded-t flex justify-between px-4 pt-5 pb-5 border-b sm:px-6 items-center bg-white transition-all"
    >
      <h3 class="text-lg leading-6 font-medium text-gray-900 flex-1">
        <component
          :is="hasTitleClickListener ? 'button' : 'span'"
          class="flex items-center w-full"
          @click="emit('title-click')"
        >
          <slot v-if="'prefix' in slots" name="prefix" />
          <span v-text="props.title" />
          <span
            v-if="props.subtitle"
            class="ml-2 text-sm text-gray-500 self-end"
            v-text="props.subtitle"
          />
          <slot v-if="'title' in slots" name="title" />
        </component>
      </h3>

      <div>
        <slot v-if="'actions' in slots" name="actions" />
        <sba-icon-button
          v-if="props.closeable"
          :icon="['far', 'times-circle']"
          @click.stop="emit('close', $event)"
        />
      </div>
    </header>
    <div
      v-if="'default' in slots"
      :class="[
        $attrs.class,
        {
          'rounded-t': !hasTitle,
          'rounded-b': !('footer' in slots),
        },
      ]"
      class="px-4 py-3 bg-white"
    >
      <div :class="{ '-mx-4 -my-3': props.seamless }">
        <sba-loading-spinner v-if="props.loading" class="" size="sm" />
        <slot v-if="!props.loading" />
      </div>
    </div>
    <footer v-if="'footer' in slots">
      <div class="px-4 py-3 border-t bg-gray-50">
        <slot name="footer" />
      </div>
    </footer>
  </div>
</template>

<script lang="ts" setup>
import { throttle } from 'lodash-es';
import {
  computed,
  defineEmits,
  defineProps,
  getCurrentInstance,
  onBeforeUnmount,
  onMounted,
  ref,
  useAttrs,
  useSlots,
} from 'vue';

import SbaIconButton from '@/components/sba-icon-button';
import SbaLoadingSpinner from '@/components/sba-loading-spinner';

import vSticksBelow from '@/directives/sticks-below';

const props = defineProps({
  title: {
    type: String,
    default: undefined,
  },
  subtitle: {
    type: String,
    default: undefined,
  },
  closeable: {
    type: Boolean,
    default: false,
  },
  headerSticksBelow: {
    type: String,
    default: undefined,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  seamless: {
    type: Boolean,
    default: false,
  },
});

const emit = defineEmits(['title-click']);

const header = ref<HTMLElement | null>(null);
const headerTopValue = ref(0);
let onScrollFn: ((this: Document, ev: Event) => any) | undefined;

const instance = getCurrentInstance();
const hasTitleClickListener = !!instance.vnode.props?.['onTitleClick'];

const slots = useSlots();

const hasTitle = computed(
  () => !!props.title || 'title' in slots || 'actions' in slots,
);

const onScroll = () => {
  if (!header.value) return;
  const boundingClientRect = header.value.getBoundingClientRect();
  if (boundingClientRect.top <= headerTopValue.value) {
    header.value.classList.add('!rounded-none', '!py-2');
  } else {
    header.value.classList.remove('!rounded-none', '!py-2');
  }
};

onMounted(() => {
  if (props.headerSticksBelow) {
    if (!header.value) return;
    headerTopValue.value = +header.value.style.top.substring(
      0,
      header.value.style.top.length - 2,
    );

    onScrollFn = throttle(onScroll, 150);
    document.addEventListener('scroll', onScrollFn);
  }
});

onBeforeUnmount(() => {
  if (props.headerSticksBelow && onScrollFn) {
    document.removeEventListener('scroll', onScrollFn);
  }
});
</script>
