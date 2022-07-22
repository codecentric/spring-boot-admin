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
    <sba-modal v-model="isModalOpen" data-testid="refreshModal">
      <template v-slot:header>
        <span v-text="$t('instances.env.context_refreshed')" />
      </template>
      <template v-slot:body>
        <span v-html="$t('instances.env.refreshed_configurations')" />
        <p v-html="refreshedPropertiesHtml" />
      </template>
      <template v-slot:footer>
        <button class="button is-success" @click="closeModal">
          OK
        </button>
      </template>
    </sba-modal>
  </sba-panel>
</template>

<script>

import Instance from '@/services/instance';
import Application from '@/services/application';
import SbaActionButtonScoped from '@/components/sba-action-button-scoped';
import SbaModal from '@/components/sba-modal';
import SbaPanel from '@/components/sba-panel';

export default {
  components: {SbaActionButtonScoped, SbaPanel, SbaModal},
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
    },
    refreshedPropertiesHtml() {
      if (this.currentScope === 'instance') {
        return '<ul class="properties-list">' + this.refreshedProperties[0].changedProperties.map(entry => ('<li>' + entry + '</li>')).join('') + '</ul>';
      } else {
        return '<ul class="properties-list">' + this.refreshedProperties.filter(property => property.changedProperties.length > 0).map(entry => ('<li>instanceId: ' + entry.instanceId + '<ul class="properties-list">' + entry.changedProperties.map(property => ('<li>' + property + '</li>')).join('') + '</ul>')).join('') + '</ul>';
      }
    }
  },
  data() {
    return {
      refreshedProperties: [],
      isModalOpen: false,
      currentScope: 'instance',
    }
  },
  methods: {
    refreshContext(scope) {
      this.currentScope = scope;
      if (scope === 'instance') {
        return this.instance.refreshContext().then((response) => {
          this.refreshedProperties = [{
            instanceId: this.instance.id,
            changedProperties: response.data,
          }];
          this.isModalOpen = response.data.length > 0;
        });
      } else {
        return this.application.refreshContext().then((response) => {
          this.refreshedProperties = response.data.map(entry =>
            (
              {
                instanceId: entry.instanceId,
                changedProperties: JSON.parse(entry.body),
              })
          );
          this.isModalOpen = this.refreshedProperties.some(props =>  props.changedProperties.length > 0);
        });
      }
    },
    closeModal() {
      this.refreshedProperties = [];
      this.isModalOpen = false;
    },
  }
}
</script>

<style lang="scss">
@import "~@/assets/css/utilities";

.properties-list {
  list-style: disc inside;
  padding: 0.5em 1em;
}

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

