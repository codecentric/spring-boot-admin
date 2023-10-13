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
  <sba-wave />
  <div class="container prose prose-slate mx-auto pt-10">
    <h1 class="mb-1" v-text="$t('about.title')" />
    <h2 v-if="version" v-text="`Version ${version}`" />
    <p>This is an administration UI for Spring Boot applications.</p>
    <p>
      To monitor applications, they must be registered at this server. This is
      either done by including the
      <a
        :href="`${documentationBaseUrl}/getting-started.html#register-clients-via-spring-boot-admin`"
        rel="noreferrer"
        target="_blank"
      >
        Spring Boot Admin Client
      </a>
      or using a
      <a
        :href="`${documentationBaseUrl}/getting-started.html#discover-clients-via-spring-cloud-discovery`"
        rel="noreferrer"
        target="_blank"
      >
        Spring Cloud Discovery Client
      </a>
      implementation.
    </p>
    <p>
      If you have any question please consult the
      <a :href="`${documentationBaseUrl}`"> Reference Guide </a>, ask on
      <a
        href="https://stackoverflow.com/questions/tagged/spring-boot-admin"
        rel="noreferrer"
        target="_blank"
        >Stack Overflow</a
      >
      or have a chat on the
      <a
        href="https://gitter.im/codecentric/spring-boot-admin"
        rel="noreferrer"
        target="_blank"
        >Gitter</a
      >
      channel.
    </p>
    <p>
      If you found a bug, want to propose a feature or submit a pull request
      please use the
      <a href="https://github.com/codecentric/spring-boot-admin/issues">
        issue tracker </a
      >.
    </p>
    <div class="flex justify-between">
      <sba-button @click="openLink(documentationBaseUrl)">
        <font-awesome-icon icon="book" size="lg" />&nbsp;Reference Guide
      </sba-button>
      <sba-button
        @click="openLink('https://github.com/codecentric/spring-boot-admin')"
      >
        <font-awesome-icon :icon="['fab', 'github']" size="lg" />&nbsp;Sources
      </sba-button>
      <sba-button
        @click="
          openLink(
            'https://stackoverflow.com/questions/tagged/spring-boot-admin',
          )
        "
      >
        <font-awesome-icon
          :icon="['fab', 'stack-overflow']"
          size="lg"
        />&nbsp;Stack Overflow
      </sba-button>
      <sba-button
        @click="openLink('https://gitter.im/codecentric/spring-boot-admin')"
      >
        <font-awesome-icon :icon="['fab', 'gitter']" size="lg" />&nbsp;Gitter
      </sba-button>
    </div>

    <h1 class="mt-5">Trademarks and Licenses</h1>
    <p>
      The source code of Spring Boot Admin is licensed under
      <a href="https://www.apache.org/licenses/LICENSE-2.0">
        Apache License 2.0 </a
      >.
    </p>
    <p>
      Spring, Spring Boot and Spring Cloud are trademarks of
      <a href="https://www.vmware.com/" rel="noreferrer" target="_blank"
        >VMware, Inc.</a
      >
      or its affiliates in the U.S. and other countries.
    </p>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

import SbaButton from '@/components/sba-button';
import SbaWave from '@/components/sba-wave';

import handle from '@/views/about/handle.vue';

export default defineComponent({
  components: { SbaWave, SbaButton },
  data: () => ({
    // eslint-disable-next-line no-undef
    version: __PROJECT_VERSION__,
  }),
  computed: {
    documentationBaseUrl() {
      return `https://codecentric.github.io/spring-boot-admin/${
        this.version || 'current'
      }`;
    },
  },
  methods: {
    openLink(url) {
      window.open(url, '_blank');
    },
  },
  install({ viewRegistry }: ViewInstallFunctionParams) {
    viewRegistry.addView({
      path: '/about',
      name: 'about',
      handle: handle,
      order: Number.MAX_VALUE,
      component: this,
    });
  },
});
</script>
