<template>
  <sba-panel :header-sticks-below="['#navigation']"
             :title="$t('instances.env.refresh')"
  >
    <sba-action-button-scoped :instance-count="instanceCount" :action-fn="refreshContext">
      <template v-slot="slotProps">
        <span v-if="slotProps.refreshStatus === 'completed'" v-text="$t('instances.env.context_refreshed')" />
        <span v-else-if="slotProps.refreshStatus === 'failed'"
              v-text="$t('instances.env.context_refresh_failed')"
        />
        <span v-else v-text="$t('instances.env.context_refresh')" />
      </template>
    </sba-action-button-scoped>
  </sba-panel>
</template>

<script>

import Instance from '@/services/instance';
import Application from '@/services/application';
import SbaActionButtonScoped from '@/components/sba-action-button-scoped';

export default {
  components: {SbaActionButtonScoped},
  props: {
    instance: {
      type: Instance,
      required: true
    },
    application: {
      type: Application,
      required: true
    },
  },
  computed: {
    instanceCount() {
      return this.application.instances.length;
    }
  },
  methods: {
    refreshContext(scope) {
      if (scope === 'instance') {
        return this.instance.refreshContext();
      } else {
        return this.application.refreshContext();
      }
    }
  }
}
</script>

<style lang="scss">
@import "~@/assets/css/utilities";

.refresh {
  &__header {
    background-color: $white;
    z-index: 10;
    padding: 0.5em 1em;
  }

  &__toggle-scope {
    width: 10em;
  }
}
</style>

