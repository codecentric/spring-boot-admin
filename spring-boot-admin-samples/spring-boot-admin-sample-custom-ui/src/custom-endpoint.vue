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
  - WITHOUT WARRANTIES OR CONDITION S OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <div class="custom">
    <p>Instance: <span v-text="instance.id" /></p>
    <p>Output: <span v-html="text" /></p>
  </div>
</template>

<script>
export default {
  props: {
    instance: {
      //<1>
      type: Object,
      required: true,
    },
  },
  data: () => ({
    text: "",
  }),
  async created() {
    console.log(this.instance);
    const response = await this.instance.axios.get("actuator/custom"); //<2>
    this.text = response.data;
  },
};
</script>

<style lang="css" scoped>
.custom {
  font-size: 20px;
  width: 80%;
}
</style>
