<!--
  - Copyright 2014-2018 the original author or authors.
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
  <sba-panel class="shadow-xl">
    <template v-if="!activeFilter">
      <div class="field">
        <p class="control has-inline-text">
          <span
            v-html="
              t('applications.suppress_notifications_on', {
                name: object.id || object.name,
              })
            "
          />&nbsp;
          <sba-select
            v-model="ttl"
            class="inline-flex"
            name="ttl"
            :options="ttlOptions"
            @click.stop
          />
        </p>
      </div>
      <div class="field is-grouped is-grouped-right">
        <div class="control">
          <sba-button
            :class="{ 'is-loading': actionState === 'executing' }"
            @click.stop="addFilter"
          >
            <font-awesome-icon icon="bell-slash" />&nbsp;<span
              v-text="t('term.suppress')"
            />
          </sba-button>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="field">
        <p class="control has-inline-text">
          <span
            v-html="
              t('applications.notifications_suppressed_for', {
                name: object.id || object.name,
              })
            "
          />&nbsp;
          <strong
            v-text="
              activeFilter.expiry
                ? activeFilter.expiry.locale(currentLocale).fromNow(true)
                : t('term.ever')
            "
          />.
        </p>
      </div>
      <div class="field is-grouped is-grouped-right">
        <div class="control">
          <sba-button
            :class="{ 'is-loading': actionState === 'executing' }"
            @click.stop="deleteActiveFilter"
          >
            <font-awesome-icon icon="bell" />&nbsp;<span
              v-text="t('term.unsuppress')"
            />
          </sba-button>
        </div>
      </div>
    </template>
  </sba-panel>
</template>
<script>
import { useI18n } from 'vue-i18n';

export default {
  props: {
    object: {
      type: Object,
      required: true,
    },
    notificationFilters: {
      type: Array,
      required: true,
    },
  },
  emits: ['filter-deleted', 'filter-added', 'filter-remove', 'filter-add'],
  setup() {
    const i18n = useI18n();
    return {
      t: i18n.t,
      currentLocale: i18n.locale,
    };
  },
  data() {
    return {
      ttl: 5 * 60 * 1000,
      ttlOptions: [
        { label: this.t('term.minutes', { count: 5 }), value: 5 * 60 * 1000 },
        { label: this.t('term.minutes', { count: 15 }), value: 15 * 60 * 1000 },
        { label: this.t('term.minutes', { count: 30 }), value: 30 * 60 * 1000 },
        { label: this.t('term.hours', { count: 1 }), value: 60 * 60 * 1000 },
        {
          label: this.t('term.hours', { count: 3 }),
          value: 3 * 60 * 60 * 1000,
        },
        {
          label: this.t('term.hours', { count: 8 }),
          value: 8 * 60 * 60 * 1000,
        },
        {
          label: this.t('term.hours', { count: 24 }),
          value: 24 * 60 * 60 * 1000,
        },
        { label: this.t('term.ever'), value: -1 },
      ],
      actionState: null,
    };
  },
  computed: {
    activeFilter() {
      return this.notificationFilters.find((f) => f.affects(this.object));
    },
  },
  methods: {
    async addFilter() {
      this.$emit('filter-add', {
        object: this.object,
        ttl: this.ttl,
      });
    },
    async deleteActiveFilter() {
      this.$emit('filter-remove', this.activeFilter);
    },
  },
};
</script>

<style>
.control.has-inline-text {
  line-height: 2.25em;
}
</style>
