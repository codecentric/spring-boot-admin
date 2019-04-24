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
  <div class="table-container">
    <table class="table is-fullwidth" v-if="servletFilters.length">
      <thead>
        <tr>
          <th v-text="$t('instances.mappings.url_pattern')" />
          <th v-text="$t('instances.mappings.servlet_name')" />
          <th v-text="$t('instances.mappings.filter_name')" />
          <th v-text="$t('instances.mappings.class')" />
        </tr>
      </thead>
      <tbody>
        <template v-for="servletFilterMapping in servletFilters">
          <tr :key="`${servletFilterMapping.name}`">
            <td>
              <div v-for="mapping in servletFilterMapping.urlPatternMappings"
                   :key="`${servletFilterMapping.name}_${mapping}`"
              >
                <code v-text="mapping" />
              </div>
            </td>
            <td>
              <div v-for="mapping in servletFilterMapping.servletNameMappings"
                   :key="`${servletFilterMapping.name}_${mapping}`"
                   v-text="mapping"
                   class="is-breakable"
              />
            </td>
            <td class="is-breakable" v-text="servletFilterMapping.name" />
            <td class="is-breakable" v-text="servletFilterMapping.className" />
          </tr>
        </template>
      </tbody>
    </table>
  </div>
</template>
<script>
  export default {
    props: {
      servletFilters: {
        type: Array,
        default: () => []
      }
    }
  }
</script>
