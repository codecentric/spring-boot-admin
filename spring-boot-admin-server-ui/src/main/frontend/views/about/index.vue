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
  <div class="section">
    <div class="container">
      <h1 class="title" v-text="$t('about.title')" />
      <h2 class="subtitle" v-if="version" v-text="`Version ${version}`" />
      <div class="content">
        Hello, this page is modified by Ömer Çelik
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    data: () => ({
      // eslint-disable-next-line no-undef
      version: __PROJECT_VERSION__
    }),
    computed: {
      documentationBaseUrl() {
        return `https://codecentric.github.io/spring-boot-admin/${this.version || 'current'}`;
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        path: '/about',
        name: 'about',
        label: 'about.label',
        order: 200,
        component: this
      });
    }
  };
</script>

<style lang="scss">
  .about-links {
    display: flex;
    align-items: center;

    & > * {
      margin-right: 0.5rem;
    }
  }

  $stackoverflow: #f48024;
  .button.is-stackoverflow.is-outlined {
    background-color: transparent;
    border-color: $stackoverflow;
    color: $stackoverflow;

    &:hover,
    &:focus {
      background-color: $stackoverflow;
      border-color: $stackoverflow;
      color: white;
    }
  }

  $gitter: #ed1965;
  .button.is-gitter.is-outlined {
    background-color: transparent;
    border-color: $gitter;
    color: $gitter;

    &:hover,
    &:focus {
      background-color: $gitter;
      border-color: $gitter;
      color: white;
    }
  }
</style>
