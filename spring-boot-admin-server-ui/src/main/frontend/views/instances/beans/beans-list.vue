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
  <table class="beans table is-fullwidth is-hoverable">
    <tbody>
      <template v-for="bean in beans">
        <tr
          class="is-selectable"
          :key="bean.name"
          @click="showDetails[bean.name] ? $delete(showDetails, bean.name) : $set(showDetails, bean.name, true)"
        >
          <td class="is-breakable">
            <span
              v-text="bean.shortName"
              :title="bean.name"
            /><br>
            <small
              class="is-muted"
              v-text="bean.shortType"
              :title="bean.type"
            />
          </td>
          <td>
            <span
              v-text="bean.scope"
              class="tag"
            />
          </td>
        </tr>
        <tr
          :key="`${bean.name}-detail`"
          v-if="showDetails[bean.name]"
        >
          <td
            colspan="2"
            class="has-background-white-ter"
          >
            <table class="table is-narrow is-fullwidth beans__bean-detail">
              <tbody>
                <tr v-if="bean.name !== bean.shortName">
                  <th>
                    <small v-text="$t('instances.beans.name')" />
                  </th>
                  <td
                    class="is-breakable"
                    v-text="bean.name"
                  />
                </tr>
                <tr
                  v-for="(alias, idx) in bean.aliases"
                  :key="alias"
                >
                  <th
                    v-if="idx === 0"
                    :rowspan="bean.aliases.length"
                  >
                    <small v-text="$t('instances.beans.aliases')" />
                  </th>
                  <td
                    class="is-breakable"
                    v-text="alias"
                  />
                </tr>
                <tr v-if="bean.type !== bean.shortType">
                  <th>
                    <small v-text="$t('instances.beans.type')" />
                  </th>
                  <td
                    class="is-breakable"
                    v-text="bean.type"
                  />
                </tr>
                <tr v-if="bean.resource">
                  <th>
                    <small v-text="$t('instances.beans.resource')" />
                  </th>
                  <td
                    class="is-breakable"
                    v-text="bean.resource"
                  />
                </tr>
                <tr
                  v-for="(dependency, idx) in bean.dependencies"
                  :key="dependency"
                >
                  <th
                    v-if="idx === 0"
                    :rowspan="bean.dependencies.length"
                  >
                    <small v-text="$t('instances.beans.dependencies')" />
                  </th>
                  <td
                    class="is-breakable"
                    v-text="dependency"
                  />
                </tr>
              </tbody>
            </table>
          </td>
        </tr>
      </template>
    </tbody>
  </table>
</template>
<script>
  export default {
    props: {
      beans: {
        type: Array,
        default: () => []
      }
    },
    data: () => ({
      showDetails: {}
    })
  }
</script>
<style lang="scss">
  @import "~@/assets/css/utilities";

  .beans {
    table.beans__bean-detail {
      tbody tr {
        pointer-events: none;
        background-color: $white-ter;
      }
    }
  }
</style>
