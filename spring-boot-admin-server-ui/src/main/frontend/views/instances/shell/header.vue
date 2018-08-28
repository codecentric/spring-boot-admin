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
  <section class="hero">
    <div class="hero-body">
      <div class="container">
        <div class="columns">
          <div class="column">
            <h1 class="title" v-if="instance" v-text="instance.registration.name"/>
            <h2 class="subtitle" v-if="instance">
              Instance <strong v-text="instance.id"/> (of <span v-text="application.instances.length"/>)
            </h2>
            <sba-tags v-if="instance" :tags="instance.tags"/>
          </div>
          <div class="column is-narrow is-hidden-mobile">
            <h2 class="subtitle is-6" v-if="instance">
              <a v-text="instance.registration.serviceUrl"
                 :href="instance.registration.serviceUrl"/><br>
              <a v-text="instance.registration.managementUrl"
                 :href="instance.registration.managementUrl"/><br>
              <a v-text="instance.registration.healthUrl"
                 :href="instance.registration.healthUrl"/>
            </h2>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
  import Application from '@/services/application';
  import Instance from '@/services/instance';

  export default {
    props: {
      instance: {
        type: Instance,
        default: null
      },
      application: {
        type: Application,
        default: null
      }
    },
  }
</script>
