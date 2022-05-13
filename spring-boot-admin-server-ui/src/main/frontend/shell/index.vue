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
  <div id="app">
    <sba-navbar
      :applications="applications"
      :error="error"
      :applications-initialized="applicationsInitialized"
    />
    <div class="mt-14">
      <router-view
        :applications="applications"
        :error="error"
        :applications-initialized="applicationsInitialized"
      />
    </div>
  </div>
</template>

<script>
import SbaNavbar from './navbar.vue';
import {useApplicationStore} from "../composables/useApplicationStore.js";

export default {
  components: {SbaNavbar},
  props: {
    error: {
      type: Error,
      default: null
    },
    applicationsInitialized: {
      type: Boolean,
      default: false
    }
  },
  setup() {
    const {applications} = useApplicationStore();

    return {applications}
  },

}
</script>
