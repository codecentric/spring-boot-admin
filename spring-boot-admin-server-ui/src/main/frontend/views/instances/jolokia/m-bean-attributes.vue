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
    <div
      v-if="application.instances.length > 1"
      class="field is-grouped control"
    >
      <sba-toggle-scope-button
        v-model="scope"
        :instance-count="application.instances.length"
      />
    </div>

    <sba-alert v-if="error" :error="error" :title="$t('term.fetch_failed')" />

    <m-bean-attribute
      v-for="(attribute, name) in mBean.attr"
      :key="`attr-${name}`"
      :descriptor="attribute"
      :name="name"
      :on-save-value="(value) => writeAttribute(name, value)"
      :value="attributeValues && attributeValues[name]"
    />
  </div>
</template>

<script>
import Application from '@/services/application';
import Instance from '@/services/instance';
import { MBean } from '@/views/instances/jolokia/MBean';
import mBeanAttribute from '@/views/instances/jolokia/m-bean-attribute';

export default {
  components: { mBeanAttribute },
  props: {
    domain: {
      type: String,
      required: true,
    },
    mBean: {
      type: MBean,
      required: true,
    },
    instance: {
      type: Instance,
      required: true,
    },
    application: {
      type: Application,
      required: true,
    },
  },
  data: () => ({
    attributeValues: null,
    error: null,
    scope: 'instance',
  }),
  computed: {},
  created() {
    this.readAttributes();
  },
  methods: {
    async readAttributes() {
      try {
        const response = await this.instance.readMBeanAttributes(
          this.domain,
          this.mBean.descriptor.raw
        );
        this.attributeValues = response.data.value;
      } catch (error) {
        console.warn('Fetching MBean attributes failed:', error);
        this.error = error;
      }
    },
    async writeAttribute(attribute, value) {
      try {
        const target =
          this.scope === 'instance' ? this.instance : this.application;
        await target.writeMBeanAttribute(
          this.domain,
          this.mBean.descriptor.raw,
          attribute,
          value
        );
      } finally {
        await this.readAttributes();
      }
    },
  },
};
</script>
