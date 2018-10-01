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
  <div style="width: 100%;">
    <sba-panel :header-sticks-below="['#navigation']"
               v-for="context in contexts" :key="context.name"
               :title="context.name">
      <table class="table is-fullwidth is-hoverable"
             v-if="context.beans.length > 0">
        <template v-for="bean in context.beans" v-if="!bean.filter(filter)">
          <tr class="is-selectable"
              :key="bean.name"
              @click="showDetails[bean.name] ? $delete(showDetails, bean.name) : $set(showDetails, bean.name, true)"
          >
            <td class="is-breakable">
              <span v-text="bean.shortName"
                    :title="bean.name"/><br>
              <small class="is-muted"
                     v-text="bean.shortType"
                     :title="bean.type"/>
            </td>
            <td><span v-text="bean.scope" class="tag"/></td>
          </tr>
          <tr :key="`${bean.name}-detail`" v-if="showDetails[bean.name]">
            <td colspan="2" class="has-background-white-ter">
              <p><span class="is-italic">Resource:</span> {{ bean.resource }}</p>
              <br>
              <span class="is-italic">Dependencies:</span>
              <ul>
                <li v-for="dependency in bean.dependencies"
                    :id="`${dependency}-${bean.name}`"
                    :key="`${dependency}-${bean.name}`">
                  {{ dependency }}
                </li>
              </ul>
            </td>
          </tr>
        </template>
      </table>
    </sba-panel>
  </div>
</template>
<script>
  export default {
    name: 'ContextsList',
    props: {
      contexts: {
        type: Array,
        default: () => []
      },
      filter: {
        type: String,
        default: ''
      }
    },
    data: () => ({
      showDetails: {}
    })
  }
</script>
