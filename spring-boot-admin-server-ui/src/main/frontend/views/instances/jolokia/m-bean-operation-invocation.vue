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
        <div class="modal-background" @click="abort"></div>
        <div class="modal-content">
            <div class="modal-card">
                <header class="modal-card-head">
                    <p class="modal-card-title" v-text="name"></p>
                </header>

                <template v-if="state === 'input-args'">
                    <section class="modal-card-body" @keyup.ctrl.enter="invoke(args)">
                        <div class="field" v-for="(arg, idx) in descriptor.args" :key="arg.name">
                            <label class="label">
                                <span v-text="arg.name"></span>
                                <small class="is-muted has-text-weight-normal" v-text="arg.type"></small>
                            </label>
                            <div class="control">
                                <input type="text" class="input" v-model="args[idx]">
                            </div>
                            <p class="help" v-text="arg.desc"></p>
                        </div>
                    </section>
                    <footer class="modal-card-foot">
                        <div class="field is-grouped is-grouped-right">
                            <div class="control">
                                <button class="button is-primary" @click="invoke(args)">Execute</button>
                            </div>
                        </div>
                    </footer>
                </template>

                <template v-else-if="state === 'executing'">
                    <section class="modal-card-body">
                        <section class="section is-loading">
                            <p>Executing...</p>
                        </section>
                    </section>
                </template>

                <template v-else-if="state === 'completed'">
                    <section class="modal-card-body">
                        <div class="message is-success">
                            <div class="message-body">
                                <strong>Execution successful.</strong>
                            </div>
                        </div>
                        <pre v-text="result"></pre>
                    </section>
                    <footer class="modal-card-foot">
                        <div class="field is-grouped is-grouped-right">
                            <div class="control">
                                <button class="button is-light" @click="abort"> Close</button>
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
                                                       icon="exclamation-triangle"></font-awesome-icon>
                                    Execution failed.
                                </strong>
                                <p v-text="error.message"></p>
                            </div>
                        </div>
                        <pre v-if="error.stacktrace"
                             v-text="error.stacktrace"></pre>
                        <pre v-if="error.response && error.response.data"
                             v-text="error.response.data"></pre>
                    </section>
                    <footer class="modal-card-foot">
                        <div class="field is-grouped is-grouped-right">
                            <div class="control">
                                <button class="button is-light" @click="abort"> Close</button>
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
    props: ['name', 'descriptor', 'onClose', 'onExecute'],
    data: () => ({
      state: null,
      error: null,
      args: null,
      result: null
    }),
    computed: {},
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
            this.result = JSON.stringify(result.data.value, null, 4);
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

    watch: {}
  }
</script>

<style lang="scss">
    @import "~@/assets/css/utilities";
</style>