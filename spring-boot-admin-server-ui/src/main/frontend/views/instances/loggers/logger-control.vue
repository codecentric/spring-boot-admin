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
  <div class="field is-grouped logger-control">
    <div class="control buttons has-addons">
      <button
        v-for="levelOption in levelOptions"
        :key="levelOption"
        class="button logger-control__level"
        :class="cssClass(levelOption)"
        v-text="levelOption"
        @click.stop="selectLevel(levelOption)"
      />
    </div>
    <div class="control">
      <button
        class="button is-light"
        :class="{ 'is-loading' : getStatusForLevel(null) === 'executing' }"
        :disabled="!isConfigured || !allowReset"
        @click.stop="selectLevel(null)"
        v-text="$t('instances.loggers.reset')"
      />
    </div>
  </div>
</template>

<script>
  export default {
    props: {
      value: {
        type: Array,
        required: true
      },
      levelOptions: {
        type: Array,
        required: true
      },
      allowReset: {
        type: Boolean,
        default: true
      },
      status: {
        type: Object,
        default: null
      }
    },
    computed: {
      isConfigured() {
        return this.value.some(l => Boolean(l.configuredLevel))
      },
    },
    methods: {
      hasEffectiveLevel(level) {
        return this.value.some(l => l.effectiveLevel === level)
      },
      hasConfiguredLevel(level) {
        return this.value.some(l => l.configuredLevel === level)
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
          'is-active is-danger': level === 'TRACE' && this.hasEffectiveLevel('TRACE'),
          'is-active is-warning': level === 'DEBUG' && this.hasEffectiveLevel('DEBUG'),
          'is-active is-info': level === 'INFO' && this.hasEffectiveLevel('INFO'),
          'is-active is-success': level === 'WARN' && this.hasEffectiveLevel('WARN'),
          'is-active is-light': level === 'ERROR' && this.hasEffectiveLevel('ERROR'),
          'is-active is-black': level === 'OFF' && this.hasEffectiveLevel('OFF'),
          'is-loading': this.getStatusForLevel(level) === 'executing'
        };
      }
    }
  }
</script>

<style lang="scss">
  .logger-control {
    &__level {
      &--inherited {
        opacity: 0.5;

        &:hover {
          opacity: 1;
        }
      }
    }
  }
</style>
