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
        <div v-if="error" class="message is-danger">
            <div class="message-body">
                <strong>
                    <font-awesome-icon class="has-text-danger" icon="exclamation-triangle"></font-awesome-icon>
                    Fetching attributes failed.
                </strong>
                <p v-text="error.message"></p>
            </div>
        </div>
        <m-bean-attribute v-for="(attribute, name) in mBean.attr" :key="`attr-${name}`"
                          :name="name" :descriptor="attribute" :value="attributeValues && attributeValues[name]"
                          :onSaveValue="value => writeAttribute(name, value)"></m-bean-attribute>
    </div>
</template>

<script>
  import mBeanAttribute from './m-bean-attribute';

  export default {
    props: ['mBean', 'domain', 'instance'],
    components: {mBeanAttribute},
    data: () => ({
      attributeValues: null,
      error: null
    }),
    computed: {},
    methods: {
      async readAttributes() {
        if (this.instance) {
          try {
            const response = await this.instance.readMBeanAttributes(this.domain, this.mBean.descriptor.raw);
            this.attributeValues = response.data.value;
          } catch (error) {
            console.warn('Fetching MBean attributes failed:', error);
            this.error = error;
          }
        }
      },
      async writeAttribute(attribute, value) {
        try {
          await this.instance.writeMBeanAttribute(this.domain, this.mBean.descriptor.raw, attribute, value);
        } finally {
          await this.readAttributes();
        }
      }
    },
    created() {
      this.readAttributes();
    },
    watch: {
      instance() {
        this.readAttributes();
      },
    }
  }
</script>

<style lang="scss">
    @import "~@/assets/css/utilities";
</style>