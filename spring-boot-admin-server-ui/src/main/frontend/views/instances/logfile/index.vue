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
    <div class="section logfile-view" :class="{ 'is-loading' : !hasLoaded }">
        <div v-if="error" class="message is-danger">
            <div class="message-body">
                <strong>
                    <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"></font-awesome-icon>
                    Fetching logfile failed.
                </strong>
                <p v-text="error.message"></p>
            </div>
        </div>
        <div class="logfile-view-actions" v-if="hasLoaded">
            <div class="logfile-view-actions__navigation">
                <sba-icon-button :disabled="atTop" @click.native="scrollToTop" icon="step-backward" size="lg"
                                 icon-class="rotated">
                </sba-icon-button>
                <sba-icon-button :disabled="atBottom" @click.native="scrollToBottom" icon="step-forward" size="lg"
                                 icon-class="rotated">
                </sba-icon-button>
            </div>
            <a class="button" v-if="instance" :href="`instances/${instance.id}/logfile`" target="_blank">
                <font-awesome-icon icon="download"></font-awesome-icon>&nbsp;Download
            </a>
        </div>
        <p v-if="skippedBytes" v-text="`skipped ${prettyBytes(skippedBytes)}`"></p>
        <!-- log will be appended here -->
    </div>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import {animationFrame, Observable} from '@/utils/rxjs';
  import _ from 'lodash';
  import prettyBytes from 'pretty-bytes';

  export default {
    props: ['instance'],
    mixins: [subscribing],
    data: () => ({
      hasLoaded: false,
      error: null,
      atBottom: true,
      atTop: false,
      skippedBytes: null
    }),
    watch: {
      async instance() {
        this.subscribe();
      }
    },
    created() {
      this.scrollParent = null;
    },
    mounted() {
      this.scrollParent = document.documentElement;
      window.addEventListener('scroll', this.onScroll);
    },
    beforeDestroy() {
      window.removeEventListener('scroll', this.onScroll);
    },
    methods: {
      prettyBytes,
      createSubscription() {
        const vm = this;
        if (this.instance) {
          vm.error = null;
          return this.instance.streamLogfile(1000)
            .do(chunk => vm.skippedBytes = vm.skippedBytes || chunk.skipped)
            .concatMap(chunk => _.chunk(chunk.addendum.split(/\r?\n/), 250))
            .map(lines => Observable.of(lines, animationFrame))
            .concatAll()
            .subscribe({
              next: lines => {
                vm.hasLoaded = true;
                lines.forEach(line => {
                  const child = document.createElement('pre');
                  child.textContent = line;
                  vm.$el.appendChild(child);
                });

                if (vm.atBottom) {
                  vm.scrollToBottom();
                }
              },
              error: error => {
                vm.hasLoaded = true;
                console.warn('Fetching logfile failed:', error);
                vm.error = error;
              }
            });
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
                margin-right: 0.5rem;
            }
        }
    }

    .rotated {
        transform: rotate(90deg);
    }
</style>