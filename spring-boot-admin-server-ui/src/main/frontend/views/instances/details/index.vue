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
                    <details-jvm :instance="instance"></details-jvm>
                </div>
                <div class="column">
                    <details-health :instance="instance"></details-health>
                    <details-datasource :instance="instance"></details-datasource>
                </div>
            </div>
        </div>
    </section>
</template>

<script>
  import detailsDatasource from './details-datasource';
  import detailsHealth from './details-health.vue';
  import detailsInfo from './details-info.vue';
  import detailsJvm from './details-jvm.vue';

  export default {
    components: {
      detailsHealth, detailsInfo, detailsJvm, detailsDatasource
    },
    props: ['instance']
  }
</script>

<style lang="scss">
</style>