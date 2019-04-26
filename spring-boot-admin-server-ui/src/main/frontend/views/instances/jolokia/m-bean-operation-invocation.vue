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
        <header class="modal-card-head">
          <p class="modal-card-title" v-text="name" />
        </header>

        <template v-if="state === 'input-args'">
          <section class="modal-card-body" @keyup.ctrl.enter="invoke(args)">
            <div class="field" v-for="(arg, idx) in descriptor.args" :key="arg.name">
              <label class="label">
                <span v-text="arg.name" />
                <small class="is-muted has-text-weight-normal" v-text="arg.type" />
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

        <template v-else-if="state === 'completed'">
          <section class="modal-card-body">
            <div class="message is-success">
              <div class="message-body">
                <strong v-text="$t('instances.jolokia.execution_successful')" />
              </div>
            </div>
            <pre v-if="descriptor.ret !== 'void'" v-text="prettyPrintedResult" />
          </section>
          <footer class="modal-card-foot">
            <div class="field is-grouped is-grouped-right">
              <div class="control">
                <button class="button is-light" @click="abort" v-text="$t('term.close')" />
              </div>
            </div>
          </footer>
        </template>

        <template v-else-if="state === 'failed'">
          <section class="modal-card-body">
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
      }
    },
    data: () => ({
      state: null,
      error: null,
      args: null,
      result: null
    }),
    computed: {
      prettyPrintedResult() {
        if (this.result && typeof this.result === 'string') {
          try {
            const o = JSON.parse(this.result);
            return JSON.stringify(o, undefined, 4);
          } catch (e) {
            return this.result;
          }
        } else if (typeof result === 'object') {
          return JSON.stringify(this.result, undefined, 4);
        }
        return this.result;
      }
    },
    methods: {
      abort() {
        this.onClose();
      },
      invoke(args) {
        this.state = (args || this.descriptor.args.length === 0) ? 'prepared' : 'input-args';
        this.args = args || new Array(this.descriptor.args.length);
        this.error = null;
        this.result = null;

        if (this.state === 'prepared') {
          this.execute()
        }
      },
      async execute() {
        this.state = 'executing';
        try {
          const result = await this.onExecute(this.args);
          if (result.data.status < 400) {
            this.result = result.data.value;
            this.state = 'completed';
          } else {
            const error = new Error(`Execution failed: ${result.data.error}`);
            error.stacktrace = result.data.stacktrace;
            this.state = 'failed';
            this.error = error;
            console.warn('Invocation failed', error);
          }
        } catch (error) {
          this.state = 'failed';
          this.error = error;
          console.warn('Invocation failed', error);
        }
      },
      keyHandler(event) {
        if (event.keyCode === 27) {
          this.abort()
        }
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
