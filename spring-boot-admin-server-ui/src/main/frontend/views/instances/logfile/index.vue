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
          <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
          <span v-text="$t('instances.logfile.fetch_failed')" />
        </strong>
        <p v-text="error.message" />
      </div>
    </div>
    <div class="logfile-view-actions" v-if="hasLoaded">
      <div class="logfile-view-actions__navigation">
        <sba-icon-button :disabled="atTop" @click="scrollToTop" icon="step-backward" size="lg"
                         icon-class="rotated"
        />
        <sba-icon-button :disabled="atBottom" @click="scrollToBottom" icon="step-forward" size="lg"
                         icon-class="rotated"
        />
      </div>
      <a class="button" :href="`instances/${instance.id}/actuator/logfile`" target="_blank">
        <font-awesome-icon icon="download" />&nbsp;
        <span v-text="$t('instances.logfile.download')" />
      </a>
    </div>
    <p v-if="skippedBytes" v-text="`skipped ${prettyBytes(skippedBytes)}`" />
    <!-- log will be appended here -->
  </div>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Instance from '@/services/instance';
  import autolink from '@/utils/autolink';
  import {animationFrameScheduler, concatAll, concatMap, map, of, tap} from '@/utils/rxjs';
  import AnsiUp from 'ansi_up';
  import chunk from 'lodash/chunk';
  import prettyBytes from 'pretty-bytes';
  import {VIEW_GROUP} from '../../index';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    mixins: [subscribing],
    data: () => ({
      hasLoaded: false,
      error: null,
      atBottom: true,
      atTop: false,
      skippedBytes: null
    }),
    created() {
      this.ansiUp = new AnsiUp();
    },
    mounted() {
      window.addEventListener('scroll', this.onScroll);
    },
    beforeDestroy() {
      window.removeEventListener('scroll', this.onScroll);
    },
    methods: {
      prettyBytes,
      createSubscription() {
        const vm = this;
        vm.error = null;
        return this.instance.streamLogfile(1000)
          .pipe(
            tap(part => vm.skippedBytes = vm.skippedBytes || part.skipped),
            concatMap(part => chunk(part.addendum.split(/\r?\n/), 250)),
            map(lines => of(lines, animationFrameScheduler)),
            concatAll()
          )
          .subscribe({
            next: lines => {
              vm.hasLoaded = true;
              lines.forEach(line => {
                const child = document.createElement('pre');
                child.innerHTML = autolink(this.ansiUp.ansi_to_html(line));
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
      },
      onScroll() {
        const scrollingEl = document.scrollingElement;
        const visibleHeight = document.documentElement.clientHeight;
        this.atBottom = visibleHeight === scrollingEl.scrollHeight - scrollingEl.scrollTop;
        this.atTop = scrollingEl.scrollTop <= 0;
      },
      scrollToTop() {
        document.scrollingElement.scrollTop = 0;
      },
      scrollToBottom() {
        document.scrollingElement.scrollTop = document.scrollingElement.scrollHeight;
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/logfile',
        parent: 'instances',
        path: 'logfile',
        component: this,
        label: 'instances.logfile.label',
        group: VIEW_GROUP.LOGGING,
        order: 200,
        isEnabled: ({instance}) => instance.hasEndpoint('logfile')
      });
    }
  }
</script>

<style lang="scss">
  @import "~@/assets/css/utilities";

  .logfile-view {
    padding: 1.5em;

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
      top: (($gap / 2) + $navbar-height-px);
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
