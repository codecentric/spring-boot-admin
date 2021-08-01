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
  <div class="field is-narrow">
    <div class="control">
      <button
        v-if="selectedScope === 'application'"
        class="button is-primary is-active"
        @click="toggleScope('instance')"
      >
        <font-awesome-icon icon="cubes" />&nbsp;
        <span v-text="$t('term.application')" />
      </button>
      <button
        v-else
        class="button"
        @click="toggleScope('application')"
      >
        <font-awesome-icon icon="cube" />&nbsp;&nbsp;
        <span v-text="$t('term.instance')" />
      </button>
    </div>
    <p class="help has-text-centered">
      <span v-if="selectedScope === 'application'"
            v-text="$t('term.affects_all_instances', {count: instanceCount})"
      />
      <span v-else v-text="$t('term.affects_this_instance_only')" />
    </p>
  </div>
</template>

<script>
export default {
  props: {
    instanceCount: {
      type: Number,
      required: true
    }
  },
  data() {
    return {
      selectedScope: 'instance'
    }
  },
  methods: {
    toggleScope(scope) {
      this.selectedScope = scope;

      this.$emit('changeScope', this.selectedScope)
    }
  }
}
</script>

<style scoped>
.button {
  width: 100%;
}
</style>
