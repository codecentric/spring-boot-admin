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
  <table class="table">
    <tr>
      <td>Id</td>
      <td v-text="changeSet.id"/>
    </tr>
    <tr>
      <td>Author</td>
      <td v-text="changeSet.author"/>
    </tr>
    <tr>
      <td>Change log</td>
      <td v-text="changeSet.changeLog"/>
    </tr>
    <tr>
      <td>Description</td>
      <td v-text="changeSet.description"/>
    </tr>
    <tr>
      <td>Execution</td>
      <td>
        <span v-text="changeSet.execType" class="tag"
              :class="execClass"/>
      </td>
    </tr>
    <tr>
      <td>Execution Date</td>
      <td v-text="changeSet.dateExecuted"/>
    </tr>
    <tr>
      <td>Execution Order</td>
      <td v-text="changeSet.orderExecuted"/>
    </tr>
    <tr>
      <td>Checksum</td>
      <td v-text="changeSet.checksum"/>
    </tr>
    <tr v-if="changeSet.comments">
      <td>Comments</td>
      <td v-text="changeSet.comments"/>
    </tr>
    <tr>
      <td>Deployment Id</td>
      <td v-text="changeSet.deploymentId"/>
    </tr>
    <tr v-if="changeSet.tag">
      <td>Tag</td>
      <td v-text="changeSet.tag"/>
    </tr>
    <tr v-if="changeSet.contexts && changeSet.contexts.length > 0">
      <td>Contexts</td>
      <td v-text="changeSet.contexts"/>
    </tr>
    <tr v-if="changeSet.labels && changeSet.labels.length > 0">
      <td>Labels</td>
      <td>
        <span v-for="label in changeSet.labels" :key="label" class="tag is-info" v-text="label"/>
      </td>
    </tr>
  </table>
</template>

<script>
  export default {
    props: {
      changeSet: {
        type: Object,
        required: true
      }
    },
    computed: {
      execClass() {
        switch (this.changeSet.execType) {
          case 'EXECUTED':
            return 'is-success';
          case 'FAILED':
            return 'is-danger';
          case 'SKIPPED':
            return 'is-light';
          case 'RERAN':
          case 'MARK_RAN':
            return 'is-warning';
          default:
            return 'is-info';
        }
      }
    }
  }
</script>
