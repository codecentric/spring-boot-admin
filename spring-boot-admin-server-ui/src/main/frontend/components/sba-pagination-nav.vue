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
  <div class="field has-addons has-addons-centered">
    <p class="control">
      <button class="button"
              :aria-label="$t('term.go_to_previous_page')"
              @click="$emit('change', current-1)"
              :disabled="current <= 1"
      >
        <span class="icon is-small">
          <font-awesome-icon :icon="['fas','angle-double-left']" />
        </span>
      </button>
    </p>
    <p class="control" v-for="(page, idx) in pageRange" :key="'page_' + idx">
      <button class="button"
              :disabled="page === skipPageString"
              :aria-label="$t('term.go_to_page_n', {page})"
              :class="{'is-active': page === current}"
              @click="() => changePage(page)"
      >
        <span v-text="page" />
      </button>
    </p>
    <p class="control">
      <button class="button"
              :aria-label="$t('term.go_to_next_page')"
              @click="$emit('change', current+1)"
              :disabled="current >= pageCount"
      >
        <span class="icon is-small">
          <font-awesome-icon :icon="['fas','angle-double-right']" />
        </span>
      </button>
    </p>
  </div>
</template>

<script>
export default {
  name: 'SbaPaginationNav',
  model: {
    prop: 'current',
    event: 'change'
  },
  props: {
    current: {type: Number, default: 1},
    pageCount: {type: Number, required: true},
    // Define amount of pages shown before and after current page.
    delta: {type: Number, default: 2}
  },
  data() {
    return {
      skipPageString: '...'
    }
  },
  methods: {
    changePage(page) {
      if (page !== this.skipPageString) {
        this.$emit('change', page)
      }
    }
  },
  computed: {
    pageRange() {
      const current = this.current;
      const delta = this.delta;
      const left = current - delta;
      const right = current + delta + 1;

      let prevPageNum;

      return Array(this.pageCount).fill(0)
        .reduce((pageNumsRemaining, cur, idx) => {
          const pageNum = idx + 1;
          if (pageNum === 1 || pageNum === this.pageCount || (pageNum >= left && pageNum < right)) {
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
    }
  }
}
</script>

<style scoped>

</style>
