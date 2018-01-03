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
    <div class="section logfile-view" :class="{ 'is-loading' : !subscription }">
        <div class="logfile-view-actions">
            <div class="logfile-view-actions__navigation">
                <sba-icon-button :disabled="atTop" @click.native="scrollToTop">
                    <font-awesome-icon icon="step-backward" size="lg" class="rotated"></font-awesome-icon>
                </sba-icon-button>
                <sba-icon-button :disabled="atBottom" @click.native="scrollToBottom">
                    <font-awesome-icon icon="step-forward" size="lg" class="rotated"></font-awesome-icon>
                </sba-icon-button>
            </div>
            <a class="button" v-if="instance" :href="`instances/${instance.id}/logfile`" target="_blank">
                <font-awesome-icon icon="download"></font-awesome-icon>&nbsp;Download
            </a>
        </div>
        <p v-if="skippedBytes > 0" v-text="skippedHumanBytes"></p>
        <!-- log will be appended here -->
    </div>
</template>

<script>
  import {animationFrame, Observable} from '@/utils/rxjs';
  import _ from 'lodash';
  import prettyBytes from 'pretty-bytes';

  export default {
    props: ['instance'],
    data: () => ({
      subscription: null,
      atBottom: true,
      atTop: false,
      skippedBytes: null,
    }),
    computed: {
      skippedHumanBytes() {
        return "skipped " + prettyBytes(this.skippedBytes);
      }
    },
    watch: {
      async instance(newVal, oldVal) {
        if (newVal !== oldVal) {
          this.stop();
          this.start();
        }
      }
    },
    created() {
      this.scrollParent = null;
      this.start();
    },
    mounted() {
      this.scrollParent = document.documentElement;
      window.addEventListener('scroll', this.onScroll);
    },
    beforeDestroy() {
      this.stop();
      window.removeEventListener('scroll', this.onScroll);
    },
    methods: {
      start() {
        const vm = this;
        if (this.instance) {
          this.subscription = this.instance.streamLogfile(1000)
            .do(chunk => vm.skippedBytes = vm.skippedBytes || chunk.skipped)
            .concatMap(chunk => _.chunk(chunk.addendum.split(/\r?\n/), 250))
            .map(lines => Observable.of(lines, animationFrame))
            .concatAll()
            .subscribe({
              next: lines => {
                lines.forEach(line => {
                  const child = document.createElement('pre');
                  child.textContent = line;
                  vm.$el.appendChild(child);
                });

                if (vm.atBottom) {
                  vm.scrollToBottom();
                }
              },
              errors: err => {
                vm.stop();
              }
            });
        }
      },
      stop() {
        if (this.subscription) {
          try {
            this.subscription.unsubscribe();
          } finally {
            this.subscription = null;
          }
        }
      },
      onScroll() {
        this.atBottom = this.scrollParent.scrollTop >= this.scrollParent.scrollHeight - this.scrollParent.clientHeight;
        this.atTop = this.scrollParent.scrollTop === 0;
      },
      scrollToTop() {
        this.scrollParent.scrollTo(this.scrollParent.scrollLeft, 0);
      },
      scrollToBottom() {
        this.scrollParent.scrollTo(this.scrollParent.scrollLeft, (this.scrollParent.scrollHeight - this.scrollParent.clientHeight))
      }
    }
  }
</script>

<style lang="scss">
    @import "~@/assets/css/utilities";

    .logfile-view {
        pre {
            word-break: break-all;
            padding: 0;
            white-space: pre-wrap;
            width: 100%;

            &:hover {
                background: $grey-lighter;
            }
        }

        &-actions {
            top: (($gap / 2) + $navbar-height-px + $tabs-height-px);
            right: ($gap /2);
            display: flex;
            align-items: center;
            position: sticky;
            float: right;

            &__navigation {
                display: inline-flex;
                flex-direction: column;
                justify-content: space-between;
            }
        }
    }

    .rotated {
        transform: rotate(90deg);
    }
</style>