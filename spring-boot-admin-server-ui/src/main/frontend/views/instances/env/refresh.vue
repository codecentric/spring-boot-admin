<template>
  <sba-panel :header-sticks-below="['#navigation']"
             :title="$t('instances.env.refresh')"
  >
    <div class="field is-horizontal">
      <div class="field-body">
        <div class="field is-grouped is-grouped-left">
          <div class="control" v-if="instanceCount > 1">
            <button
              v-if="currentScope === 'application'"
              class="refresh__toggle-scope button is-primary is-active"
              @click="setScope('instance')"
            >
              <font-awesome-icon icon="cubes"/>&nbsp;
              <span v-text="$t('instances.env.application')"/>
            </button>
            <button
              v-else
              class="refresh__toggle-scope button"
              @click="setScope('application')"
            >
              <font-awesome-icon icon="cube"/>&nbsp;&nbsp;
              <span v-text="$t('instances.env.instance')"/>
            </button>
            <p class="help has-text-centered">
          <span v-if="currentScope === 'application'"
                v-text="$t('instances.env.affects_all_instances', {count: instanceCount})"/>
              <span v-else v-text="$t('instances.env.affects_this_instance_only')"/>
            </p>
          </div>
          <div class="control">
            <div class="field has-icons-left">
              <sba-confirm-button class="button is-light"
                                  :class="{'is-loading' : refreshStatus === 'executing', 'is-danger' : refreshStatus === 'failed', 'is-info' : refreshStatus === 'completed'}"
                                  :disabled="refreshStatus === 'executing'"
                                  @click="refreshContext(currentScope)"
              >
                <span v-if="refreshStatus === 'completed'" v-text="$t('instances.env.context_refreshed')"/>
                <span v-else-if="refreshStatus === 'failed'" v-text="$t('instances.env.context_refresh_failed')"/>
                <span v-else v-text="$t('instances.env.context_refresh')"/>
              </sba-confirm-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </sba-panel>
</template>

<script>

import Instance from '@/services/instance';
import {from, listen} from '@/utils/rxjs';
import Application from '@/services/application';

export default {
  props: {
    instance: {
      type: Instance,
      required: true
    },
    application: {
      type: Application,
      required: true
    },
    instanceCount: {
      type: Number,
      required: true
    }
  },
  data: () => ({
    refreshStatus: null,
    currentScope: 'instance'
  }),
  methods: {
    refreshContext(currentScope) {
      const vm = this;
      let observable;
      if (currentScope === 'instance') {
        observable = from(vm.instance.refreshContext());
      } else {
        observable = from(vm.application.refreshContext());
      }
      observable
        .pipe(listen(status => vm.refreshStatus = status))
        .subscribe({
          complete: () => {
            setTimeout(() => vm.refreshStatus = null, 2500);
            return vm.$emit('reset');
          },
          error: () => vm.$emit('reset')
        });
    },
    setScope(scope) {
      this.currentScope = scope;
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

