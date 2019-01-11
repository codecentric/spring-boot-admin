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
  <table class="table is-hoverable is-selectable is-fullwidth instances-list">
    <tbody>
      <tr v-for="instance in instances" :key="instance.id" @click.stop="showDetails(instance)">
        <td class="instance-list-item__status">
          <sba-status :status="instance.statusInfo.status" :date="instance.statusTimestamp" />
        </td>
        <td class="is-narrow">
          <a v-text="instance.registration.serviceUrl || instance.registration.healthUrl"
             :href="instance.registration.serviceUrl || instance.registration.healthUrl"
             @click.stop
          /><br>
          <span class="is-muted" v-text="instance.id" />
        </td>
        <td>
          <sba-tags :tags="instance.tags" />
        </td>
        <td>
          <span v-text="instance.buildVersion" />
        </td>
        <td class="instance-list-item__actions">
          <slot name="actions" :instance="instance" />
        </td>
      </tr>
    </tbody>
  </table>
</template>
<script>
  export default {
    props: {
      instances: {
        type: Array,
        default: () => [],
      },
      showNotificationSettings: {
        type: Boolean,
        default: false
      },
      hasActiveNotificationFilter: {
        type: Function,
        default: () => false
      }
    },
    methods: {
      showDetails(instance) {
        this.$router.push({name: 'instances/details', params: {instanceId: instance.id}});
      }
    }
  }
</script>
<style lang="scss">
  @import "~@/assets/css/utilities";

  .instances-list td {
    vertical-align: middle;
  }

  .instance-list-item {
    &__status {
      width: $gap;
    }

    &__actions {
      text-align: right;
      opacity: 0;
      transition: all $easing $speed;
      will-change: opacity;
      margin-right: $gap;

      *:hover > & {
        opacity: 1;
      }

      & > * {
        width: ($gap / 2);
        height: ($gap / 2);
      }
    }
  }
</style>
