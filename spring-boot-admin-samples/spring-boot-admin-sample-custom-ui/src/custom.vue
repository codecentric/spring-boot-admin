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
  <div class="m-4">
    <template v-for="application in applications" :key="application.name">
      <sba-panel :title="application.name">
        This application has the following instances:

        <ul>
          <template v-for="instance in application.instances">
            <li>
              <span class="mx-1" v-text="instance.registration.name"></span>

              <!-- SBA components are registered globally and can be used without importing them! -->
              <!-- They are defined in spring-boot-admin-server-ui -->
              <sba-status :status="instance.statusInfo.status" class="mx-1" />
              <sba-tag :value="instance.id" class="mx-1" label="id" />
            </li>
          </template>
        </ul>
      </sba-panel>
    </template>
    <pre v-text="applications"></pre>
  </div>
</template>

<script>
/* global SBA */
import "./custom.css";

export default {
  setup() {
    const { applications } = SBA.useApplicationStore(); //<1>
    return {
      applications,
    };
  },
  methods: {
    stringify: JSON.stringify,
  },
};
</script>
