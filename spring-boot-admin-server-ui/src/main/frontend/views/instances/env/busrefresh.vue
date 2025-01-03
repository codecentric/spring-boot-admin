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
        v-text="t('instances.env.context_refreshed')"
      />
      <span
        v-else-if="slotProps.refreshStatus === 'failed'"
        v-text="t('instances.env.context_refresh_failed')"
      />
      <span v-else v-text="t('instances.env.bus_refresh')" />
    </template>
  </sba-confirm-button>
</template>

<script lang="ts">
import { useNotificationCenter } from '@stekoe/vue-toast-notificationcenter';
import { useI18n } from 'vue-i18n';

import SbaConfirmButton from '@/components/sba-confirm-button.vue';

import Instance from '@/services/instance';

const notificationCenter = useNotificationCenter({});

export default {
  components: { SbaConfirmButton },
  props: {
    instance: {
      type: Instance,
      required: true,
    },
  },
  emits: ['refresh'],
  setup() {
    const i18n = useI18n();
    return {
      t: i18n.t,
    };
  },
  data() {
    return {
      refreshedProperties: [],
      isModalOpen: false,
    };
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
        notificationCenter.success(this.t('instances.env.bus_refresh_success'));
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
