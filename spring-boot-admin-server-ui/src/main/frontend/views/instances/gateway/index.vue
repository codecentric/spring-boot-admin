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
    <global-filters :instance="instance" />
    <routes :instance="instance" />
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import globalFilters from './global-filters';
  import routes from './routes';
  import {VIEW_GROUP} from '../../index';

  export default {
    components: {
      globalFilters,
      routes
    },
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/gateway',
        parent: 'instances',
        path: 'gateway',
        component: this,
        label: 'instances.gateway.label',
        group: VIEW_GROUP.WEB,
        order: 960,
        isEnabled: ({instance}) => instance.hasEndpoint('gateway')
      });
    }
  }
</script>
