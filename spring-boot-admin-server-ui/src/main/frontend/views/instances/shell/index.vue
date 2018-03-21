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
  <div>
    <sba-instance-header :instance="instance" :application="application" :class="headerClass"/>
    <sba-instance-tabs :views="instanceViews" :instance="instance" :application="application" :class="headerClass"/>
    <router-view v-if="instance" :instance="instance"/>
  </div>
</template>

<script>
  import sbaInstanceHeader from './header';
  import sbaInstanceTabs from './tabs';

  export default {
    components: {sbaInstanceHeader, sbaInstanceTabs},
    props: {
      instanceId: {
        type: String,
        required: true
      },
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
      instance() {
        return this.applications.findInstance(this.instanceId);
      },
      application() {
        return this.applications.findApplicationForInstance(this.instanceId);
      },
      instanceViews() {
        return this.$root.views.filter(view => view.name.lastIndexOf('instance/') === 0);
      },
      headerClass() {
        if (!this.instance) {
          return '';
        }
        if (this.instance.statusInfo.status === 'UP') {
          return 'is-primary';
        }
        if (this.instance.statusInfo.status === 'RESTRICTED') {
          return 'is-warning';
        }
        if (this.instance.statusInfo.status === 'DOWN') {
          return 'is-danger';
        }
        if (this.instance.statusInfo.status === 'OUT_OF_SERVICE') {
          return 'is-danger';
        }
        if (this.instance.statusInfo.status === 'OFFLINE') {
          return 'is-light';
        }
        return 'is-light';
      }
    }
  }
</script>
