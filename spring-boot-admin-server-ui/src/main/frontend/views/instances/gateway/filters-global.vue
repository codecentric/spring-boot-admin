<template>
  <section class="section" :class="{ 'is-loading' : !hasLoaded }">
    <div v-if="error" class="message is-danger">
      <div class="message-body">
        <strong>
          <font-awesome-icon class="has-text-danger" icon="exclamation-triangle" />
          Fetching global filters failed.
        </strong>
        <p v-text="error.message" />
      </div>
    </div>

    <div class="field has-addons" v-if="globalFilters">
      <p class="control is-expanded">
        <input class="input" type="search" placeholder="name filter" v-model="globalFilterFilter">
      </p>
    </div>

    <!-- TODO: Global filters panel -->

  </section>
</template>

<script>
  import Instance from '@/services/instance';

  export default {
    props: {
      instance: {
        type: Instance,
        required: true
      }
    },
    data: () => ({
      hasLoaded: false,
      error: null,
      globalFilters: null,
      globalFilterFilter: null
    }),
    computed: {
      hasGlobalFiltersData: function () {
        return this.globalFilters && this.globalFilters.length;
      }
    },
    created() {
      this.fetchGlobalFiltersData();
    },
    methods: {
      async fetchGlobalFiltersData() {
        this.error = null;
        try {
          const res = await this.instance.fetchGlobalFiltersData();
          this.globalFilters = res.data;
        } catch (error) {
          console.warn('Fetching global filters failed:', error);
          this.error = error;
        }
        this.hasLoaded = true;
      }
    }
  }
</script>
