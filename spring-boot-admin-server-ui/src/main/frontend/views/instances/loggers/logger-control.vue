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
  <div class="inline-flex">
    <div class="btn-group">
      <sba-button
        v-for="levelOption in levelOptions"
        :key="levelOption"
        :class="cssClass(levelOption)"
        @click.stop="selectLevel(levelOption)"
        v-text="levelOption"
      />
    </div>
    <sba-button
      class="ml-3"
      :class="{ 'is-loading': getStatusForLevel(null) === 'executing' }"
      :disabled="!isConfigured || !allowReset"
      @click.stop="selectLevel(null)"
      v-text="$t('instances.loggers.reset')"
    />
  </div>
</template>

<script>
export default {
  props: {
    value: {
      type: Array,
      required: true,
    },
    levelOptions: {
      type: Array,
      required: true,
    },
    allowReset: {
      type: Boolean,
      default: true,
    },
    status: {
      type: Object,
      default: null,
    },
  },
  emits: ['input'],
  computed: {
    isConfigured() {
      return this.value.some((l) => Boolean(l.configuredLevel));
    },
  },
  methods: {
    hasEffectiveLevel(level) {
      return this.value.some((l) => {
        if (this.isLoggingGroup(l)) {
          return this.hasConfiguredLevel(level);
        } else {
          return l.effectiveLevel === level;
        }
      });
    },
    hasConfiguredLevel(level) {
      return this.value.some((l) => l.configuredLevel === level);
    },
    isLoggingGroup(logger) {
      return !!logger.members;
    },
    selectLevel(level) {
      this.$emit('input', level);
    },
    getStatusForLevel(level) {
      if (this.status && this.status.level === level) {
        return this.status.status;
      }
    },
    cssClass(level) {
      return {
        'logger-control__level--inherited': !this.hasConfiguredLevel(level),
        'is-active is-danger':
          level === 'TRACE' && this.hasEffectiveLevel('TRACE'),
        'is-active is-warning':
          level === 'DEBUG' && this.hasEffectiveLevel('DEBUG'),
        'is-active is-info': level === 'INFO' && this.hasEffectiveLevel('INFO'),
        'is-active is-success':
          level === 'WARN' && this.hasEffectiveLevel('WARN'),
        'is-active is-light':
          level === 'ERROR' && this.hasEffectiveLevel('ERROR'),
        'is-active is-extra-light':
          level === 'FATAL' && this.hasEffectiveLevel('FATAL'),
        'is-active is-black': level === 'OFF' && this.hasEffectiveLevel('OFF'),
        'is-loading': this.getStatusForLevel(level) === 'executing',
      };
    },
  },
};
</script>

<style>
.logger-control__level--inherited {
  @apply bg-opacity-70 !important;
}
</style>
