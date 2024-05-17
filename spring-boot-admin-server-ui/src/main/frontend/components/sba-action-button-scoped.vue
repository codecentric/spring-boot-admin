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
  <div class="inline-flex rounded items-start -space-x-px">
    <sba-toggle-scope-button
      v-if="instanceCount > 1"
      v-model="currentScope"
      class="!rounded-r-none relative focus:z-10"
      :instance-count="instanceCount"
      :show-info="showInfo"
      @update:model-value="emitScopeChanged"
    />
    <sba-confirm-button
      class="inline-flex focus:z-10"
      :class="{ '!rounded-l-none': instanceCount > 1 }"
      :disabled="disabled"
      @click="click"
    >
      <slot v-if="label">
        <span v-if="refreshStatus === 'completed'" v-text="labelCompleted" />
        <span v-else-if="refreshStatus === 'failed'" v-text="labelFailed" />
        <div v-else-if="refreshStatus === 'executing'">
          <svg
            class="inline w-4 h-4 text-gray-900 animate-spin"
            viewBox="0 0 100 101"
            fill="none"
            xmlns="http://www.w3.org/2000/svg"
          >
            <path
              d="M100 50.5908C100 78.2051 77.6142 100.591 50 100.591C22.3858 100.591 0 78.2051 0 50.5908C0 22.9766 22.3858 0.59082 50 0.59082C77.6142 0.59082 100 22.9766 100 50.5908ZM9.08144 50.5908C9.08144 73.1895 27.4013 91.5094 50 91.5094C72.5987 91.5094 90.9186 73.1895 90.9186 50.5908C90.9186 27.9921 72.5987 9.67226 50 9.67226C27.4013 9.67226 9.08144 27.9921 9.08144 50.5908Z"
              fill="#E5E7EB"
            />
            <path
              d="M93.9676 39.0409C96.393 38.4038 97.8624 35.9116 97.0079 33.5539C95.2932 28.8227 92.871 24.3692 89.8167 20.348C85.8452 15.1192 80.8826 10.7238 75.2124 7.41289C69.5422 4.10194 63.2754 1.94025 56.7698 1.05124C51.7666 0.367541 46.6976 0.446843 41.7345 1.27873C39.2613 1.69328 37.813 4.19778 38.4501 6.62326C39.0873 9.04874 41.5694 10.4717 44.0505 10.1071C47.8511 9.54855 51.7191 9.52689 55.5402 10.0491C60.8642 10.7766 65.9928 12.5457 70.6331 15.2552C75.2735 17.9648 79.3347 21.5619 82.5849 25.841C84.9175 28.9121 86.7997 32.2913 88.1811 35.8758C89.083 38.2158 91.5421 39.6781 93.9676 39.0409Z"
              fill="currentColor"
            />
          </svg>
        </div>
        <span v-else v-text="label" />
      </slot>
      <slot v-else :refresh-status="refreshStatus" />
    </sba-confirm-button>
  </div>
</template>

<script>
import { finalize } from 'rxjs';
import { useI18n } from 'vue-i18n';

import { ActionScope } from '@/components/ActionScope';

import { from, listen } from '@/utils/rxjs';

export default {
  props: {
    instanceCount: {
      type: Number,
      required: true,
    },
    actionFn: {
      type: Function,
      required: true,
    },
    disabled: { type: Boolean, default: false },
    label: {
      type: String,
      default: undefined,
    },
    labelFailed: {
      type: String,
      default() {
        const { tm } = useI18n();
        return tm('term.execution_failed');
      },
    },
    labelCompleted: {
      type: String,
      default() {
        const { tm } = useI18n();
        return tm('term.execution_successful');
      },
    },
    showInfo: {
      type: Boolean,
      default: true,
    },
  },
  emits: ['scopeChanged'],
  data() {
    return {
      status: null,
      refreshStatus: '',
      currentScope: ActionScope.INSTANCE,
    };
  },
  methods: {
    resetRefreshState() {
      setTimeout(() => {
        this.refreshStatus = null;
      }, 2000);
    },
    click() {
      this.refreshStatus = 'executing';

      from(this.actionFn(this.currentScope))
        .pipe(
          listen((status) => (this.refreshStatus = status)),
          finalize(() => this.resetRefreshState()),
        )
        .subscribe();
    },
    emitScopeChanged(scope) {
      this.$emit('scopeChanged', scope);
    },
  },
};
</script>
