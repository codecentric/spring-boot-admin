<template>
  <sba-confirm-button
    :title="$t('instances.env.bus_refresh_title')"
    class="inline-flex focus:z-10"
    @click="refreshInstance"
  >
    <span v-text="t('instances.env.bus_refresh')" />
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
  methods: {
    async refreshInstance() {
      this.instance
        .busRefreshContext()
        .then(() => {
          notificationCenter.success(
            this.t('instances.env.bus_refresh_success'),
          );
          this.$emit('refresh', true);
        })
        .catch(() => {
          notificationCenter.error(this.t('instances.env.bus_refresh_failure'));
        });
    },
  },
};
</script>
