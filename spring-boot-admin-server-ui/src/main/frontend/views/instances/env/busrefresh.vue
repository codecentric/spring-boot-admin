<template>
  <sba-confirm-button
    :title="$t('instances.env.bus_refresh_title')"
    class="inline-flex focus:z-10"
    @click="refreshInstance"
  >
    <template #default="slotProps">
      <span
        v-if="slotProps.refreshStatus === 'completed'"
        class="is-success"
        v-text="$t('instances.env.context_refreshed')"
      />
      <span
        v-else-if="slotProps.refreshStatus === 'failed'"
        v-text="$t('instances.env.context_refresh_failed')"
      />
      <span v-else v-text="$t('instances.env.bus_refresh')" />
    </template>
  </sba-confirm-button>

  <sba-modal v-model="isModalOpen" data-testid="refreshModal">
    <template #header>
      <span v-text="$t('instances.env.context_refreshed')" />
    </template>
    <template #body>
      <span v-html="$t('instances.env.refreshed_configurations')" />
      <p v-html="refreshedPropertiesHtml" />
    </template>
    <template #footer>
      <button class="button is-success" @click="closeModal">
        {{ $t('term.ok') }}
      </button>
    </template>
  </sba-modal>
</template>

<script>
import SbaConfirmButton from '@/components/sba-confirm-button.vue';
import SbaModal from '@/components/sba-modal.vue';

import Instance from '@/services/instance';

export default {
  components: { SbaModal, SbaConfirmButton },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  emits: ['refresh'],
  data() {
    return {
      refreshedProperties: [],
      isModalOpen: false,
    };
  },
  computed: {
    refreshedPropertiesHtml() {
      if (this.refreshedProperties.length > 0) {
        return (
          '<ul class="properties-list">' +
          this.refreshedProperties[0].changedProperties
            .map((entry) => '<li>' + entry + '</li>')
            .join('') +
          '</ul>'
        );
      } else {
        return (
          '<ul class="properties-list">' +
          this.refreshedProperties
            .filter((property) => property.changedProperties.length > 0)
            .map(
              (entry) =>
                '<li>instanceId: ' +
                entry.instanceId +
                '<ul class="properties-list">' +
                entry.changedProperties
                  .map((property) => '<li>' + property + '</li>')
                  .join('') +
                '</ul>',
            )
            .join('') +
          '</ul>'
        );
      }
    },
  },
  methods: {
    async refreshInstance() {
      await this.instance.busRefreshContext().then((response) => {
        this.refreshedProperties = [
          {
            instanceId: this.instance.id,
            changedProperties: response.data,
          },
        ];
        this.$emit('refresh', this.refreshedProperties.length > 0);
      });
    },
    closeModal() {
      this.refreshedProperties = [];
      this.isModalOpen = false;
    },
  },
};
</script>
