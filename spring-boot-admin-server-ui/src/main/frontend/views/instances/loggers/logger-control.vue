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
      <button class="button is-light" :class="{ 'is-loading' : isLoading === null }"
              :disabled="!configured || !allowReset" @click.stop="selectLevel(null)"
      >
        Reset
      </button>
    </div>
  </div>
</template>

<script>
  export default {
    props: {
      effectiveLevel: {
        type: String,
        required: true
      },
      configuredLevel: {
        type: String,
        default: null
      },
      levelOptions: {
        type: Array,
        required: true
      },
      allowReset: {
        type: Boolean,
        default: true
      },
      isLoading: {
        type: String,
        default: undefined
      }
    },
    computed: {
      level() {
        return this.configuredLevel || this.effectiveLevel;
      },
      configured() {
        return !!this.configuredLevel;
      }
    },
    methods: {
      selectLevel(level) {
        this.$emit('input', level);
      },
      cssClass(level) {
        return {
          'logger-control__level--inherited': this.level === level && !this.configured,
          'is-active is-danger': this.level === level && this.level === 'TRACE',
          'is-active is-warning': this.level === level && this.level === 'DEBUG',
          'is-active is-info': this.level === level && this.level === 'INFO',
          'is-active is-success': this.level === level && this.level === 'WARN',
          'is-active is-light': this.level === level && this.level === 'ERROR',
          'is-active is-black': this.level === level && this.level === 'OFF',
          'is-loading': this.isLoading === level
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
