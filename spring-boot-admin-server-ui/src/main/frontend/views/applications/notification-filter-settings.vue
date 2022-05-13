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
  <sba-panel>
    <template v-if="!activeFilter">
      <div class="field">
        <p class="control has-inline-text">
          <span v-html="$t('applications.suppress_notifications_on', {name: object.id || object.name})" />&nbsp;
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
            :class="{'is-loading' : actionState === 'executing'}"
            @click.stop="addFilter"
          >
            <font-awesome-icon icon="bell-slash" />&nbsp;<span v-text="$t('term.suppress')" />
          </sba-button>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="field">
        <p class="control has-inline-text">
          <span v-html="$t('applications.notifications_suppressed_for', {name: object.id || object.name})" />&nbsp;
          <strong
            v-text="activeFilter.expiry ? activeFilter.expiry.locale(currentLocale).fromNow(true) : $t('term.ever') "
          />.
        </p>
      </div>
      <div class="field is-grouped is-grouped-right">
        <div class="control">
          <sba-button
            :class="{'is-loading' : actionState === 'executing'}"
            @click.stop="deleteActiveFilter"
          >
            <font-awesome-icon icon="bell" />&nbsp;<span v-text="$t('term.unsuppress')" />
          </sba-button>
        </div>
      </div>
    </template>
  </sba-panel>
</template>
<script>
import NotificationFilter from '@/services/notification-filter';
import {useI18n} from "vue-i18n";

export default {
  props: {
    object: {
      type: Object,
      required: true
    },
    notificationFilters: {
      type: Array,
      required: true
    }
  },
  emits: ['filter-deleted', 'filter-added'],
  setup() {
    const i18n = useI18n();
    return {
      i18n
    }
  },
  data() {
    return {
      ttl: 5 * 60 * 1000,
      ttlOptions: [
        {label: this.i18n.t('term.minutes', 5, {count: 5}), value: 5 * 60 * 1000},
        {label: this.i18n.t('term.minutes', 15, {count: 15}), value: 15 * 60 * 1000},
        {label: this.i18n.t('term.minutes', 30, {count: 30}), value: 30 * 60 * 1000},
        {label: this.i18n.t('term.hours', 1, {count: 1}), value: 60 * 60 * 1000},
        {label: this.i18n.t('term.hours', 3, {count: 3}), value: 3 * 60 * 60 * 1000},
        {label: this.i18n.t('term.hours', 8, {count: 8}), value: 8 * 60 * 60 * 1000},
        {label: this.i18n.t('term.hours', 24, {count: 24}), value: 24 * 60 * 60 * 1000},
        {label: this.i18n.t('term.ever'), value: -1}
      ],
      actionState: null
    };
  },
  computed: {
    activeFilter() {
      return this.notificationFilters.find(f => f.affects(this.object));
    },
    currentLocale() {
      return this.$i18n.locale;
    }
  },
  methods: {
    async addFilter() {
      this.actionState = 'executing';
      try {
        const response = await NotificationFilter.addFilter(this.object, this.ttl);
        this.actionState = 'completed';
        this.$emit('filter-added', response.data)
      } catch (error) {
        console.warn('Adding notification filter failed:', error);
      }
    },
    async deleteActiveFilter() {
      this.actionState = 'executing';
      try {
        await this.activeFilter.delete();
        this.actionState = 'completed';
        this.$emit('filter-deleted', this.activeFilter.id);
      } catch (error) {
        this.actionState = 'failed';
        console.warn('Deleting notification filter failed:', error);
      }
    }
  }
}
</script>

<style lang="css">
.control.has-inline-text {
  line-height: 2.25em;
}
</style>
