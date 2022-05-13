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
  <section class="wallboard section">
    <p
      v-if="!applicationsInitialized"
      class="is-muted is-loading"
    >
      Loading applications...
    </p>
    <hex-mesh
      v-if="applicationsInitialized"
      :items="applications"
      :class-for-item="classForApplication"
      @click="select"
    >
      <template #item="{item: application}">
        <div
          :key="application.name"
          class="hex__body application"
        >
          <div class="application__header application__time-ago is-muted">
            <sba-time-ago :date="application.statusTimestamp" />
          </div>
          <div class="application__body">
            <h1
              class="application__name"
              v-text="application.name"
            />
            <p
              class="application__instances is-muted"
              v-text="$tc('wallboard.instances_count', application.instances.length)"
            />
          </div>
          <h2
            class="application__footer application__version"
            v-text="application.buildVersion"
          />
        </div>
      </template>
    </hex-mesh>
  </section>
</template>

<script>
  import hexMesh from './hex-mesh.vue';
  import {HealthStatus} from '../../HealthStatus.js';
  import {useApplicationStore} from "../../composables/useApplicationStore.js";

  export default {
    components: {hexMesh},
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
    methods: {
      classForApplication(application) {
        if (!application) {
          return null;
        }
        if (application.status === HealthStatus.UP) {
          return 'up';
        }
        if (application.status === HealthStatus.RESTRICTED) {
          return 'restricted';
        }
        if (application.status === HealthStatus.DOWN) {
          return 'down';
        }
        if (application.status === HealthStatus.OUT_OF_SERVICE) {
          return 'down';
        }
        if (application.status === HealthStatus.OFFLINE) {
          return '';
        }
        return '';
      },
      select(application) {
        if (application.instances.length === 1) {
          this.$router.push({name: 'instances/details', params: {instanceId: application.instances[0].id}});
        } else {
          this.$router.push({name: 'applications', params: {selected: application.name}});
        }
      }
    },
    install({viewRegistry}) {
      viewRegistry.addView({
        path: '/wallboard',
        name: 'wallboard',
        label: 'wallboard.label',
        order: -100,
        component: this
      });
    }
  };
</script>

<style lang="postcss">
.wallboard {
  background-color: #4a4a4a;
  height: calc(100vh - 52px);
  width: 100%;
}
.wallboard .application {
  color: #f5f5f5;
  font-size: 1em;
  font-weight: 400;
  line-height: 1;
  text-align: center;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.wallboard .application__name {
  width: 100%;
  padding: 2.5%;
  color: #fff;
  font-size: 2em;
  font-weight: 600;
  line-height: 1.125;
}
.wallboard .application__version {
  color: #f5f5f5;
  font-size: 1.25em;
  line-height: 1.25;
}
.wallboard .application__header {
  width: 90%;
  margin-bottom: 0.5em;
}
.wallboard .application__footer {
  width: 90%;
  margin-top: 0.5em;
}

.up > polygon {
  stroke: theme('colors.green.400');
  fill:  theme('colors.green.400');
}

.down > polygon {
  stroke: theme('colors.red.400');
  fill:  theme('colors.red.400');
}

.restricted > polygon {
  stroke: theme('colors.yellow.400');
  fill:  theme('colors.yellow.400');
}
</style>
