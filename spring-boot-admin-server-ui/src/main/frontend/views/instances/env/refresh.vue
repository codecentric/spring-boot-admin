<template>
  <sba-action-button-scoped
    :instance-count="instanceCount"
    :action-fn="refreshContext"
    :show-info="false"
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
      <span v-else v-text="$t('instances.env.context_refresh')" />
    </template>
  </sba-action-button-scoped>

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
import { ActionScope } from '@/components/ActionScope';

import Application from '@/services/application';
import Instance from '@/services/instance';

export default {
  props: {
    instance: {
      type: Instance,
      required: true,
    },
    application: {
      type: Application,
      required: true,
    },
  },
  emits: ['refresh'],
  data() {
    return {
      refreshedProperties: [],
      isModalOpen: false,
      currentScope: ActionScope.INSTANCE,
    };
  },
  computed: {
    instanceCount() {
      return this.application.instances.length;
    },
    refreshedPropertiesHtml() {
      if (
        this.currentScope === 'instance' &&
        this.refreshedProperties.length > 0
      ) {
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
      await this.instance.refreshContext().then((response) => {
        this.refreshedProperties = [
          {
            instanceId: this.instance.id,
            changedProperties: response.data,
          },
        ];
        this.isModalOpen = response.data.length > 0;
      });
    },
    async refreshApplication() {
      await this.application.refreshContext().then((response) => {
        this.refreshedProperties = response.data.map((entry) => ({
          instanceId: entry.instanceId,
          changedProperties: JSON.parse(entry.body),
        }));
        this.isModalOpen = this.refreshedProperties.some(
          (props) => props.changedProperties.length > 0,
        );
      });
    },
    async refreshContext(scope) {
      this.currentScope = scope;
      if (scope === 'instance') {
        await this.refreshInstance();
      } else {
        await this.refreshApplication();
      }

      this.$emit('refresh', this.refreshedProperties.length > 0);
    },
    closeModal() {
      this.refreshedProperties = [];
      this.isModalOpen = false;
    },
  },
};
</script>
