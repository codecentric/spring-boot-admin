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
  <div class="box">
    <template v-if="!activeFilter">
      <div class="field">
        <p class="control has-inline-text">
          Suppress notifications on <code v-text="object.id || object.name" /> for
          <span class="select">
            <select v-model="ttl" @click.stop>
              <option v-for="option in ttlOptions"
                      :key="`ttl-instance-${option.value}`"
                      :value="option.value" v-text="option.label"
              />
            </select>
          </span>
        </p>
      </div>
      <div class="field is-grouped is-grouped-right">
        <div class="control">
          <button class="button is-warning" :class="{'is-loading' : actionState === 'executing'}"
                  @click.stop="addFilter"
          ><font-awesome-icon icon="bell-slash" />&nbsp;Suppress
          </button>
        </div>
      </div>
    </template>
    <template v-else>
      <div class="field">
        <p class="control has-inline-text">
          Notifications on <code v-text="object.id || object.name" /> are suppressed for
          <strong v-text="activeFilter.expiry ? activeFilter.expiry.locale('en').fromNow(true) : 'ever' " />.
        </p>
      </div>
      <div class="field is-grouped is-grouped-right">
        <div class="control">
          <button class="button" :class="{'is-loading' : actionState === 'executing'}" @click.stop="deleteActiveFilter">
            <font-awesome-icon icon="bell" />&nbsp;Unsuppress
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
<script>
  import NotificationFilter from '@/services/notification-filter';

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
    data: () => ({
      ttl: 5 * 60 * 1000,
      ttlOptions: [
        {label: '5 minutes', value: 5 * 60 * 1000},
        {label: '15 minutes', value: 15 * 60 * 1000},
        {label: '30 minutes', value: 30 * 60 * 1000},
        {label: '1 hour', value: 60 * 60 * 1000},
        {label: '3 hours', value: 3 * 60 * 60 * 1000},
        {label: '8 hours', value: 8 * 60 * 60 * 1000},
        {label: '24 hours', value: 24 * 60 * 60 * 1000},
        {label: 'ever', value: -1}
      ],
      actionState: null
    }),
    computed: {
      activeFilter() {
        return this.notificationFilters.find(f => f.affects(this.object));
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

<style lang="scss">
  .control.has-inline-text {
    line-height: 2.25em;
  }
</style>
