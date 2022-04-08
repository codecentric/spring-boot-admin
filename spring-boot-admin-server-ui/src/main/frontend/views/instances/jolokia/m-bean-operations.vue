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
    <div class="field is-grouped control" v-if="application.instances.length > 1">
      <sba-toggle-scope-button :instance-count="application.instances.length" v-model="scope" />
    </div>

    <mBeanOperation v-for="(operation, name) in mBean.op" :key="`op-${name}`"
                    :name="name" :descriptor="operation" @click="invoke(name, operation)"
    />
    <m-bean-operation-invocation v-if="invocation" :name="invocation.name" :descriptor="invocation.descriptor"
                                 :on-execute="execute" :on-close="closeInvocation"
    />
  </div>
</template>

<script>
  import Application from '@/services/application';
  import Instance from '@/services/instance';
  import {MBean} from './index';
  import mBeanOperation from './m-bean-operation';
  import mBeanOperationInvocation from './m-bean-operation-invocation';
  import SbaToggleScopeButton from '@/components/sba-toggle-scope-button';

  export default {
    props: {
      domain: {
        type: String,
        required: true
      },
      mBean: {
        type: MBean,
        required: true
      },
      application: {
        type: Application,
        required: true
      },
      instance: {
        type: Instance,
        required: true
      }
    },
    components: {mBeanOperation, mBeanOperationInvocation, SbaToggleScopeButton},
    data: () => ({
      invocation: null,
      scope: 'instance'
    }),
    methods: {
      closeInvocation() {
        this.invocation = null
      },
      invoke(name, descriptor) {
        this.invocation = {name, descriptor};
      },
      execute(args) {
        const target = (this.scope === 'instance') ? this.instance : this.application;
        return target.invokeMBeanOperation(this.domain, this.mBean.descriptor.raw, this.invocation.name, args);
      }
    }
  }
</script>
