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
  <div class="field is-horizontal">
    <div class="field-body">
      <sba-toggle-scope-button v-if="instanceCount > 1"
                               :instance-count="instanceCount"
                               v-model="currentScope"
      />
      <div class="field has-icons-left">
        <sba-confirm-button class="button is-light"
                            :class="{'is-loading' : refreshStatus === 'executing', 'is-danger' : refreshStatus === 'failed', 'is-info' : refreshStatus === 'completed'}"
                            :disabled="disabled"
                            @click="click"
        >
          <slot v-if="label">
            <span v-if="refreshStatus === 'completed'" v-text="labelCompleted" />
            <span v-else-if="refreshStatus === 'failed'" v-text="labelFailed" />
            <span v-else v-text="label" />
          </slot>
          <slot v-else :refresh-status="refreshStatus" />
        </sba-confirm-button>
      </div>
    </div>
  </div>
</template>

<script>

import {from, listen} from '@/utils/rxjs';
import SbaToggleScopeButton from '@/components/sba-toggle-scope-button';
import SbaConfirmButton from '@/components/sba-confirm-button';

export default {
  components: {SbaConfirmButton, SbaToggleScopeButton},
  props: {
    instanceCount: {
      type: Number,
      required: true
    },
    actionFn: {
      type: Function,
      required: true
    },
    disabled: {type: Boolean, default: false},
    label: {
      type: String,
      default: undefined
    },
    labelFailed: {
      type: String,
      default() {
        return this.$t('term.execution_failed')
      }
    },
    labelCompleted: {
      type: String,
      default() {
        return this.$t('term.execution_successful')
      }
    },
  },
  data: () => ({
    status: null,
    refreshStatus: null,
    currentScope: 'instance'
  }),
  methods: {
    setScope(scope) {
      this.currentScope = scope;
    },
    click() {
      from(this.actionFn(this.currentScope))
        .pipe(listen(status => this.refreshStatus = status))
        .subscribe({
          complete: () => setTimeout(() => this.refreshStatus = null, 2500)
        });
    }
  }
}
</script>

<style lang="scss">
@import "~@/assets/css/utilities";

.refresh {
  &__header {
    background-color: $white;
    z-index: 10;
    padding: 0.5em 1em;
  }

  &__toggle-scope {
    width: 10em;
  }
}
</style>

