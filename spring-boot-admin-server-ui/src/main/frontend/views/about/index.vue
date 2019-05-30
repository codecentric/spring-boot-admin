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
        <p>
          This is an administration UI for Spring Boot applications.
        </p>
        <p>
          To monitor applications, they must be registered at this server. This is either done by including the
          <a
            :href="`${documentationBaseUrl}/#register-clients-via-spring-boot-admin`"
          >
            Spring Boot Admin Client
          </a>
          or using a
          <a
            :href="`${documentationBaseUrl}/#discover-clients-via-spring-cloud-discovery`"
          >
            Spring Cloud Discovery Client
          </a> implementation.
        </p>
        <p>
          If you have any question please consult the
          <a :href="`${documentationBaseUrl}`">
            Reference Guide
          </a>, ask
          on <a href="https://stackoverflow.com/questions/tagged/spring-boot-admin">
            Stack Overflow
          </a> or
          have a chat on the <a href="https://gitter.im/codecentric/spring-boot-admin">
            Gitter
          </a> channel.
        </p>
        <p>
          If you found a bug, want to propose a feature or submit a pull request please use the
          <a href="https://github.com/codecentric/spring-boot-admin/issues">
            issue tracker
          </a>.
        </p>
        <div class="about-links">
          <a class="button is-primary is-outlined"
             :href="`${documentationBaseUrl}`"
          >
            <font-awesome-icon size="lg" icon="book" />&nbsp;Reference Guide
          </a>
          <a class="button is-black is-outlined" href="https://github.com/codecentric/spring-boot-admin">
            <font-awesome-icon size="lg" :icon="['fab', 'github']" />&nbsp;Sources
          </a>
          <a class="button is-stackoverflow is-outlined"
             href="https://stackoverflow.com/questions/tagged/spring-boot-admin"
          >
            <font-awesome-icon size="lg" :icon="['fab', 'stack-overflow']" />&nbsp;Stack Overflow
          </a>
          <a class="button is-gitter is-outlined" href="https://gitter.im/codecentric/spring-boot-admin">
            <font-awesome-icon size="lg" :icon="['fab', 'gitter']" />&nbsp;Gitter
          </a>
        </div>
      </div>
      <h1 class="title is-5">
        Trademarks and Licenses
      </h1>
      <div class="content">
        <p>
          The source code of Spring Boot Admin is licensed under <a
            href="https://www.apache.org/licenses/LICENSE-2.0"
          >
            Apache License 2.0
          </a>.
        </p>
        <p>
          Spring, Spring Boot and Spring Cloud are trademarks of <a href="https://pivotal.io/">
            Pivotal
            Software, Inc.
          </a> in the U.S. and other countries.
        </p>
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
