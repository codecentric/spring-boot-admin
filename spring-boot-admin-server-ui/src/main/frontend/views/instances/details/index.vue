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
            <div class="columns">
                <div class="column">
                    <details-info :instance="instance"></details-info>
                </div>
                <div class="column">
                    <details-health :instance="instance"></details-health>
                </div>
            </div>
            <div class="columns">
                <div class="column">
                    <details-process :instance="instance"></details-process>
                    <details-memory :instance="instance" type="heap"></details-memory>
                </div>
                <div class="column">
                    <details-threads :instance="instance"></details-threads>
                    <details-memory :instance="instance" type="nonheap"></details-memory>
                </div>
            </div>
            <div class="columns">
                <div class="column">
                    <details-datasources :instance="instance"></details-datasources>
                </div>
                <div class="column">
                </div>
            </div>
        </div>
    </section>
</template>

<script>
  import detailsDatasources from './details-datasources';
  import detailsHealth from './details-health';
  import detailsInfo from './details-info';
  import detailsMemory from './details-memory';
  import detailsProcess from './details-process';
  import detailsThreads from './details-threads';

  export default {
    components: {
      detailsHealth, detailsInfo, detailsProcess, detailsThreads, detailsDatasources, detailsMemory
    },
    props: ['instance']
  }
</script>

<style lang="scss">
</style>