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
    <section class="section">
        <div class="container">
            <div v-if="error" class="message is-danger">
                <div class="message-body">
                    <strong>
                        <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"></font-awesome-icon>
                        Fetching metrics index failed.
                    </strong>
                </div>
            </div>
            <div class="columns is-desktop">
                <div class="column is-half">
                    <details-info v-if="hasInfo" :instance="instance"></details-info>
                </div>
                <div class="column is-half">
                    <details-health :instance="instance"></details-health>
                </div>
            </div>
            <div class="columns is-desktop">
                <div class="column is-half">
                    <details-process v-if="hasProcess" :instance="instance"></details-process>
                    <details-gc v-if="hasGc" :instance="instance"></details-gc>
                </div>
                <div class="column is-half">
                    <details-threads v-if="hasThreads" :instance="instance"></details-threads>
                </div>
            </div>
            <div class="columns is-desktop">
                <div class="column is-half">
                    <details-memory v-if="hasMemory" :instance="instance" type="heap"></details-memory>
                </div>
                <div class="column is-half">
                    <details-memory v-if="hasMemory" :instance="instance" type="nonheap"></details-memory>
                </div>
            </div>
            <div class="columns is-desktop">
                <div class="column is-half">
                    <details-datasources v-if="hasDatasources" :instance="instance"></details-datasources>
                </div>
                <div class="column is-half">
                    <details-caches v-if="hasCaches" :instance="instance"></details-caches>
                </div>
            </div>
        </div>
    </section>
</template>

<script>
  import detailsCaches from './details-caches';
  import detailsDatasources from './details-datasources';
  import detailsGc from './details-gc';
  import detailsHealth from './details-health';
  import detailsInfo from './details-info';
  import detailsMemory from './details-memory';
  import detailsProcess from './details-process';
  import detailsThreads from './details-threads';

  export default {
    components: {
      detailsHealth,
      detailsInfo,
      detailsProcess,
      detailsThreads,
      detailsDatasources,
      detailsMemory,
      detailsGc,
      detailsCaches
    },
    props: ['instance'],
    data: () => ({
      hasLoaded: false,
      error: null,
      metrics: []
    }),
    computed: {
      hasCaches() {
        return this.metrics.indexOf('cache.requests') >= 0;
      },
      hasDatasources() {
        return this.metrics.indexOf('data.source.active.connections') >= 0;
      },
      hasGc() {
        return this.metrics.indexOf('jvm.gc.pause') >= 0;
      },
      hasInfo() {
        return this.instance && this.instance.hasEndpoint('info');
      },
      hasMemory() {
        return this.metrics.indexOf('jvm.memory.max') >= 0;
      },
      hasProcess() {
        return this.metrics.indexOf('process.uptime') >= 0;
      },
      hasThreads() {
        return this.metrics.indexOf('jvm.threads.live') >= 0;
      },
    },
    created() {
      this.fetchMetricIndex();
    },
    watch: {
      instance() {
        this.fetchMetricIndex();
      }
    },
    methods: {
      async fetchMetricIndex() {
        if (this.instance && this.instance.hasEndpoint('metrics')) {
          this.error = null;
          try {
            const res = await this.instance.fetchMetrics();
            this.metrics = res.data.names;
          } catch (error) {
            console.warn('Fetching metric index failed:', error);
            this.error = error;
          }
          this.hasLoaded = true;
        }
      }
    }
  }
</script>