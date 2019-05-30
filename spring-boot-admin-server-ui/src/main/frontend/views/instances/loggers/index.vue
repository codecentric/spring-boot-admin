<!--
  - Copyright 2014-2019 the original author or authors.
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
  <loggers
    :loggers-service="service"
    :scope="scope"
    :instance-count="application.instances.length"
    @changeScope="s => scope = s"
  />
</template>

<script>
  import Application from '@/services/application';
  import Instance from '@/services/instance';
  import Loggers from './loggers';
  import {ApplicationLoggers, InstanceLoggers} from './service';
  import {VIEW_GROUP} from '../../index';

  export default {
    components: {Loggers},
    props: {
      instance: {
        type: Instance,
        required: true
      },
      application: {
        type: Application,
        required: true
      }
    },
    data: function () {
      return {
        scope: this.application.instances.length > 1 ? 'application' : 'instance'
      };
    },
    computed: {
      service() {
        return this.scope === 'instance'
          ? new InstanceLoggers(this.instance)
          : new ApplicationLoggers(this.application);
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        name: 'instances/loggers',
        parent: 'instances',
        path: 'loggers',
        label: 'instances.loggers.label',
        component: this,
        group: VIEW_GROUP.LOGGING,
        order: 300,
        isEnabled: ({instance}) => instance.hasEndpoint('loggers')
      });
    }
  }
</script>
