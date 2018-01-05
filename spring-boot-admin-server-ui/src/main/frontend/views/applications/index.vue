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
    <section class="section">
        <div class="container">
            <div class="level">
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Applications</p>
                        <p class="title" v-text="applicationsCount">1</p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div>
                        <p class="heading">Instances</p>
                        <p class="title" v-text="instancesCount">1</p>
                    </div>
                </div>
                <div class="level-item has-text-centered">
                    <div v-if="downCount === 0">
                        <p class="heading">Status</p>
                        <p class="title has-text-success">all up</p>
                    </div>
                    <div v-else>
                        <p class="heading">instances down</p>
                        <p class="title has-text-danger" v-text="downCount"></p>
                    </div>
                </div>
            </div>
            <div v-for="group in statusGroups" :key="group.status" class="content">
                <p class="heading" v-text="group.status"></p>
                <applications-list :applications="group.applications"></applications-list>
            </div>
            <div v-if="statusGroups.length === 0"
                 class="content">
                <p class="is-muted">No applications registered.</p>
            </div>
        </div>
    </section>
</template>

<script>
  import subscribing from '@/mixins/subscribing';
  import Application from '@/services/application'
  import * as _ from 'lodash';
  import applicationsList from './applications-list.vue'

  export default {
    mixins: [subscribing],
    components: {
      applicationsList,
    },
    data: () => ({
      _applications: [],
      errors: []
    }),
    computed: {
      applications() {
        return this.$data._applications.filter(application => application.instances.length > 0);
      },
      statusGroups() {
        const byStatus = _.groupBy(this.applications, application => application.status);
        const list = _.transform(byStatus, (result, value, key) => {
          result.push({status: key, applications: value})
        }, []);
        return _.sortBy(list, [item => item.status]);
      },
      applicationsCount() {
        return this.applications.length;
      },
      instancesCount() {
        return this.applications.reduce((current, next) => current + next.instances.length, 0);
      },
      downCount() {
        return this.applications.reduce((current, next) => {
          return current + (next.instances.filter(instance => instance.statusInfo.status !== 'UP').length);
        }, 0);
      }
    },
    methods: {
      async createSubscription() {
        try {
          this.$data._applications = (await Application.list()).data;
        } catch (e) {
          this.errors.push(e);
        }

        return (await Application.getStream()).subscribe({
          next: message => {
            const idx = this.$data._applications.findIndex(application => application.name === message.data.name);
            if (idx >= 0) {
              this.$data._applications.splice(idx, 1, message.data);
            } else {
              this.$data._applications.push(message.data);
            }
          },
          error: err => this.errors.push(err)
        });
      }
    }
  }
</script>

<style lang="scss">
</style>