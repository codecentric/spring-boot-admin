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
    <h1 v-text="t('custom_sub.hello')"></h1>
  </div>
</template>

<script setup>
/* global:SBA */
const { t, mergeLocaleMessage } = SBA.useI18n();

mergeLocaleMessage("en", {
  custom_sub: {
    hello: "Hello from the submenu item",
  },
});
mergeLocaleMessage("de", {
  custom_sub: {
    hello: "Hallo, ich bin ein Sub-Menü-Element!",
  },
});
</script>

<style lang="css" scoped>
.custom {
  font-size: 20px;
  width: 80%;
}
</style>
