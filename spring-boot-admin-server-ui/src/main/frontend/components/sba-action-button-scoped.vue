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
      <btn-scope v-if="instanceCount > 1"
                 :instance-count="instanceCount"
                 :scope="currentScope"
                 @changeScope="setScope"
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
          <slot :refresh-status="refreshStatus" v-else />
        </sba-confirm-button>
      </div>
    </div>
  </div>
</template>

<script>

import BtnScope from '@/components/btn-scope';
import {from, listen} from '@/utils/rxjs';

export default {
  components: {BtnScope},
  props: {
    instanceCount: {
      type: Number,
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
    actionFn: {
      type: Function,
      required: true
    }
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
          complete: () => {
            setTimeout(() => this.refreshStatus = null, 2500);
            return this.$emit('reset');
          },
          error: () => this.$emit('reset')
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

