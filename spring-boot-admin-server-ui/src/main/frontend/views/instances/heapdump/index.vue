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
  <section class="section heapdump">
    <div>
      <div class="message is-warning">
        <div class="message-body" v-html="$t('instances.heapdump.warn_sensitive_data')" />
      </div>
      <div class="message is-warning">
        <div class="message-body" v-html="$t('instances.heapdump.warn_dump_expensive')" />
      </div>
      <a class="button is-primary" :href="`instances/${instance.id}/actuator/heapdump`" target="_blank">
        <font-awesome-icon icon="download" />&nbsp;
        <span v-text="$t('instances.heapdump.download')" />
      </a>
    </div>
  </section>
</template>

<script>
  import Instance from '@/services/instance';
  import {VIEW_GROUP} from '../../index';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/heapdump',
        parent: 'instances',
        path: 'heapdump',
        component: this,
        label: 'instances.heapdump.label',
        group: VIEW_GROUP.JVM,
        order: 800,
        isEnabled: ({instance}) => instance.hasEndpoint('heapdump')
      });
    }
  }
</script>

<style lang="scss">
  .heapdump {
    display: flex;
    justify-content: space-around;

    & > div {
      display: flex;
      flex-direction: column;
    }
  }
</style>
