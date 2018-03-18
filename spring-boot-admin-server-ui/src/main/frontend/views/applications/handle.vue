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
  <span>
    <span class="has-text-warning" v-if="error">
      <font-awesome-icon icon="exclamation-triangle"/>
    </span>
    <span :class="{ 'badge is-badge-danger' : downCount > 0 }" :data-badge="downCount">
      Applications
    </span>
  </span>
</template>

<script>
  import logoDanger from '@/assets/img/favicon-danger.png';
  import logoOk from '@/assets/img/favicon.png';

  export default {
    props: {
      applications: {
        type: Array,
        default: () => [],
      },
      error: {
        type: null,
        default: null
      }
    },
    computed: {
      downCount() {
        return this.applications.reduce((current, next) => {
          return current + (next.instances.filter(instance => instance.statusInfo.status !== 'UP').length);
        }, 0);
      }
    },
    watch: {
      downCount(newVal, oldVal) {
        if ((newVal === 0) !== (oldVal === 0)) {
          this.updateFavicon(newVal === 0);
        }
      }
    },
    methods: {
      updateFavicon(up) {
        document.querySelector('link[rel*="icon"]').href = up ? logoOk : logoDanger;
      }
    }
  };
</script>
