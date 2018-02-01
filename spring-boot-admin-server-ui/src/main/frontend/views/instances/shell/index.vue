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
        <sba-instance-header :instance="instance"></sba-instance-header>
        <sba-instance-tabs :views="instanceViews" :instance="instance"></sba-instance-tabs>
        <router-view :instance="instance"></router-view>
    </div>
</template>

<script>
  import instance from '@/services/instance'
  import sbaInstanceHeader from './header';
  import sbaInstanceTabs from './tabs';

  export default {
    components: {sbaInstanceHeader, sbaInstanceTabs},
    props: ['instanceId'],
    data: () => ({
      instance: null
    }),
    computed: {
      instanceViews() {
        return this.$root.views.filter(view => view.name.lastIndexOf('instance/') === 0);
      }
    },
    async created() {
      const res = await instance.get(this.instanceId);
      this.instance = res.data;
    }
  }
</script>