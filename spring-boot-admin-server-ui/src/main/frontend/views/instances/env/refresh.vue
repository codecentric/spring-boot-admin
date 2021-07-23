<template>
    <sba-panel :header-sticks-below="['#navigation']"
               :title="$t('instances.env.refresh')"
    >
      <div class="field is-horizontal">
        <div class="field-body" v-if="instance.hasEndpoint('refresh')">
          <div class="field">
            <div class="control">
              <sba-confirm-button class="button is-light"
                                  :class="{'is-loading' : refreshStatus === 'executing', 'is-danger' : refreshStatus === 'failed', 'is-info' : refreshStatus === 'completed'}"
                                  :disabled="refreshStatus === 'executing'"
                                  @click="refreshContext"
              >
                <span v-if="refreshStatus === 'completed'" v-text="$t('instances.env.context_refreshed')"/>
                <span v-else-if="refreshStatus === 'failed'" v-text="$t('instances.env.context_refresh_failed')"/>
                <span v-else v-text="$t('instances.env.context_refresh')"/>
              </sba-confirm-button>
            </div>
          </div>
        </div>
      </div>
    </sba-panel>
</template>

<script>

import Instance from '@/services/instance';
import {from, listen} from '@/utils/rxjs';

export default {
  props: {
    instance: {
      type: Instance,
      required: true
    }
  },
  data: () => ({
    refreshStatus: null,
  }),
  methods: {
    refreshContext() {
      const vm = this;
      from(vm.instance.refreshContext())
        .pipe(listen(status => vm.refreshStatus = status))
        .subscribe({
          complete: () => {
            setTimeout(() => vm.refreshStatus = null, 2500);
            return vm.$emit('reset');
          },
          error: () => vm.$emit('reset')
        });
    },
  }
}
</script>
