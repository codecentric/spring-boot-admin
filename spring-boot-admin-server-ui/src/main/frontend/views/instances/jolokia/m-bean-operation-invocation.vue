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
  <div class="modal is-active">
    <div class="modal-background" @click="abort" />
    <div class="modal-content">
      <div class="modal-card">
        <header class="modal-card-head is-block">
          <p class="modal-card-title" v-text="name" />
        </header>

        <template v-if="state === 'input-args'">
          <section class="modal-card-body" @keyup.ctrl.enter="invoke(args)">
            <div class="field" v-for="(arg, idx) in descriptor.args" :key="arg.name">
              <label class="label">
                <span v-text="arg.name" />
                <small class="is-muted has-text-weight-normal pl-1" v-text="arg.type" />
              </label>
              <div class="control">
                <input type="text" class="input" v-model="args[idx]">
              </div>
              <p class="help" v-text="arg.desc" />
            </div>
          </section>
          <footer class="modal-card-foot">
            <div class="field is-grouped is-grouped-right">
              <div class="control">
                <button class="button is-primary" @click="invoke(args)" v-text="$t('instances.jolokia.execute')" />
              </div>
            </div>
          </footer>
        </template>

        <template v-else-if="state === 'executing'">
          <section class="modal-card-body">
            <section class="section is-loading">
              <p v-text="$t('instances.jolokia.executing')" />
            </section>
          </section>
        </template>

        <template v-else-if="scope === 'instance'">
          <section class="modal-card-body">
            <template v-if="state === 'completed'">
              <div class="message is-success">
                <div class="message-body">
                  <strong v-text="$t('instances.jolokia.execution_successful')" />
                </div>
              </div>
              <pre v-if="descriptor.ret !== 'void'" v-text="prettyPrinted(result)" />
            </template>

            <template v-else-if="state === 'failed'">
              <div class="message is-danger">
                <div class="message-body">
                  <strong>
                    <font-awesome-icon class="has-text-danger"
                                       icon="exclamation-triangle"
                    />
                    <span v-text="$t('instances.jolokia.execution_failed')" />
                  </strong>
                  <p v-text="error.message" />
                </div>
              </div>
              <pre v-if="error.stacktrace"
                   v-text="error.stacktrace"
              />
              <pre v-if="error.response && error.response.data"
                   v-text="error.response.data"
              />
            </template>
          </section>
          <footer class="modal-card-foot">
            <div class="field is-grouped is-grouped-right">
              <div class="control">
                <button class="button is-light" @click="abort" v-text="$t('instances.jolokia.close')" />
              </div>
            </div>
          </footer>
        </template>

        <template v-else-if="scope === 'application'">
          <section class="modal-card-body">
            <div v-for="(instanceId, idx) in instanceIds" :key="instanceId">
              <template v-if="state[idx] === 'completed'">
                <div class="message is-success instance">
                  <div :class="{'is-selectable': descriptor.ret !== 'void'}"
                       class="message-body"
                       @click="select(instanceId)"
                  >
                    <strong v-text="$t('term.instance') + ': ' + instanceId" />
                  </div>
                  <pre v-if="selectedInstance === instanceId && descriptor.ret !== 'void'" v-text="prettyPrinted(result[idx])" />
                </div>
              </template>

              <template v-else-if="state[idx] === 'failed'">
                <div class="message is-danger">
                  <div class="message-body">
                    <strong>
                      <font-awesome-icon class="has-text-danger"
                                         icon="exclamation-triangle"
                      />
                      <span v-text="$t('term.instance') + ': ' + instanceId" />
                    </strong>
                    <p v-text="error[idx].message" />
                  </div>
                </div>
                <pre v-if="error[idx].stacktrace"
                     v-text="error[idx].stacktrace"
                />
                <pre v-if="error[idx].response && error[idx].response.data"
                     v-text="error[idx].response.data"
                />
              </template>
            </div>
          </section>

          <footer class="modal-card-foot">
            <div class="field is-grouped is-grouped-right">
              <div class="control">
                <button class="button is-light" @click="abort" v-text="$t('instances.jolokia.close')" />
              </div>
            </div>
          </footer>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
import {
  responseHandler,
  STATE_EXECUTING, STATE_FAILED,
  STATE_INPUT_ARGS,
  STATE_PREPARED
} from '@/views/instances/jolokia/responseHandler.js';
import {sortBy} from 'lodash'

export default {
  props: {
    name: {
      type: String,
      required: true
    },
    descriptor: {
      type: Object,
      required: true
    },
    value: {
      type: null,
      default: null
    },
    onClose: {
      type: Function,
      required: true
    },
    onExecute: {
      type: Function,
      required: true
    },
    scope: {
      type: String,
      required: true
    }
  },
  data: () => ({
    state: null,
    error: null,
    args: null,
    result: null,
    instanceIds: null,
    selectedInstance: null
  }),
  methods: {
    abort() {
      this.onClose();
    },
    invoke(args) {
      this.state = (args || this.descriptor.args.length === 0) ? STATE_PREPARED : STATE_INPUT_ARGS;
      this.args = args || new Array(this.descriptor.args.length);
      this.error = null;
      this.result = null;

      if (this.state === STATE_PREPARED) {
        this.execute()
      }
    },
    async execute() {
      this.state = STATE_EXECUTING;
      try {
        const response = await this.onExecute(this.args);
        if (this.scope === 'instance') {
          const {result, state, error} = responseHandler(response);
          this.result = result;
          this.state = state;
          this.error = error;
        } else {
          const handledResponse = sortBy(responseHandler(response), [
            r => (r.state === STATE_FAILED) ? -1 : 1,
            r => r.instanceId
          ]);
          this.instanceIds = handledResponse.map(r => r.instanceId);
          this.result = handledResponse.map(r => r.result);
          this.state = handledResponse.map(r => r.state);
          this.error = handledResponse.map(r => r.error);
        }
      } catch (error) {
        this.state = STATE_FAILED;
        this.error = error;
        console.warn('Invocation failed', error);
      }
    },
    keyHandler(event) {
      if (event.keyCode === 27) {
        this.abort()
      }
    },
    prettyPrinted(result) {
      if (result && typeof result === 'string') {
        try {
          const o = JSON.parse(result);
          return JSON.stringify(o, undefined, 4);
        } catch (e) {
          return result;
        }
      } else if (typeof result === 'object') {
        return JSON.stringify(result, undefined, 4);
      }
      return result;
    },
    select(instanceId) {
      this.selectedInstance = (this.selectedInstance !== instanceId) ? instanceId : null;
    }
  },
  created() {
    this.invoke();
  },
  mounted() {
    document.addEventListener('keyup', this.keyHandler)
  },
  beforeDestroy() {
    document.removeEventListener('keyup', this.keyHandler)
  },
}
</script>

<style scoped lang="scss">
@import "~@/assets/css/utilities";

.modal-card-title {
  word-break: break-all;
}

.is-selectable {

  &:hover {
    background-color: $white-bis;
  }

}

</style>
