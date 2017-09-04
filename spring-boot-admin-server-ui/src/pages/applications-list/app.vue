<template>
    <div class="instances-list">
        <div v-for="item in statusGroups" :key="item.status"
             class="instances-list__status-section">
            <h3 v-text="item.status"></h3>
            <div v-for="group in item.groups" :key="group.name"
                 class="instances-list__group">
                <span v-text="group.name"></span>
                <span v-if="group.versions.length === 1" v-text="group.versions[0]"></span>
                <span v-else-if="group.versions.length > 1">{{group.versions.length}} different versions</span>
                <span v-text="group.status"></span>
                <span v-text="group.statusTimestamp"></span>
            </div>
        </div>
    </div>
</template>

<script>
  import instances from '../../services/instances'
  import * as _ from 'lodash';

  export default {
    data: () => ({
      groups: [],
      errors: [],
    }),

    computed: {
      statusGroups() {
        const byStatus = _.groupBy(this.groups, group => group.status);
        const list = _.transform(byStatus, (result, value, key) => {
          result.push({status: key, groups: value})
        }, []);
        return _.sortBy(list, [item => item.status]);
      }
    },

    async created() {
      try {
        this.groups = await instances.getGroups();
      } catch (e) {
        this.errors.push(e);
      }
    }
  }
</script>

<style lang="scss">
    .instances-list {
        &__status-section {

        }

        &__group {
        }
    }
</style>
