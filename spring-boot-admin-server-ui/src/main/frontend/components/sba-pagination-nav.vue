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
  <div>
    <sba-button-group
      class="relative z-0 btn-group rounded shadow-sm -space-x-px"
      aria-label="Pagination"
    >
      <sba-button :disabled="modelValue <= 1" @click="goPrev()">
        <span class="sr-only" v-text="$t('term.go_to_previous_page')" />
        <font-awesome-icon
          class="h-5 w-5"
          :icon="['fas', 'angle-double-left']"
        />
      </sba-button>
      <sba-button
        v-for="(page, idx) in pageRange"
        :key="'page_' + idx"
        :aria-hidden="page === skipPageString"
        :aria-current="page === modelValue"
        :disabled="page === skipPageString || page === modelValue"
        :class="{
          'is-active': page === modelValue,
          'cursor-not-allowed': page === skipPageString,
        }"
        @click="() => changePage(page)"
      >
        <span class="sr-only">
          <span
            v-if="page !== modelValue"
            v-text="$t('term.go_to_page_n', { page })"
          />
          <span v-else v-text="$t('term.current_page', { page })" />
        </span>

        <span aria-hidden="true" v-text="page" />
      </sba-button>

      <sba-button :disabled="modelValue >= pageCount" @click="goNext">
        <span class="sr-only" v-text="$t('term.go_to_next_page')" />
        <font-awesome-icon
          class="h-5 w-10"
          :icon="['fas', 'angle-double-right']"
        />
      </sba-button>
    </sba-button-group>
  </div>
</template>

<script>
import SbaButton from '@/components/sba-button';
import SbaButtonGroup from '@/components/sba-button-group';

export default {
  name: 'SbaPaginationNav',
  components: { SbaButton, SbaButtonGroup },
  props: {
    modelValue: { type: Number, default: 1 },
    pageCount: { type: Number, required: true },
    // Define amount of pages shown before and after current page.
    delta: { type: Number, default: 2 },
  },
  emits: ['update:modelValue'],
  data() {
    return {
      skipPageString: '...',
    };
  },
  computed: {
    pageRange() {
      const pageCount = this.pageCount <= 0 ? 1 : this.pageCount;
      const current = this.modelValue;
      const delta = this.delta;
      const left = current - delta;
      const right = current + delta + 1;

      let prevPageNum;

      return Array(pageCount)
        .fill(0)
        .reduce((pageNumsRemaining, cur, idx) => {
          const pageNum = idx + 1;
          if (
            pageNum === 1 ||
            pageNum === pageCount ||
            (pageNum >= left && pageNum < right)
          ) {
            pageNumsRemaining.push(pageNum);
          }
          return pageNumsRemaining;
        }, [])
        .reduce((paginationNavEntries, pageNum) => {
          if (prevPageNum) {
            if (pageNum - prevPageNum === 2) {
              paginationNavEntries.push(prevPageNum + 1);
            } else if (pageNum - prevPageNum !== 1) {
              paginationNavEntries.push(this.skipPageString);
            }
          }
          paginationNavEntries.push(pageNum);
          prevPageNum = pageNum;
          return paginationNavEntries;
        }, []);
    },
  },
  methods: {
    goPrev() {
      const newPage = this.modelValue - 1;
      if (newPage > 0) {
        this.$emit('update:modelValue', newPage);
      }
    },
    goNext() {
      const newPage = this.modelValue + 1;
      if (newPage <= this.pageCount) {
        this.$emit('update:modelValue', newPage);
      }
    },
    changePage(page) {
      if (page !== this.skipPageString) {
        this.$emit('update:modelValue', page);
      }
    },
  },
};
</script>

<style scoped>
.is-active {
  @apply bg-indigo-50 border border-indigo-500 z-10 !important;
  @apply font-extrabold;
}
</style>
