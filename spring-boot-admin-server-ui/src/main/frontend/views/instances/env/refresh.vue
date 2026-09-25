<template>
  <sba-action-button-scoped
    :instance-count="instanceCount"
    :action-fn="refreshContext"
    :show-info="false"
    :label="$t('instances.env.context_refresh')"
  >
    <template #default>
      {{ $t('instances.env.context_refresh') }}
    </template>
    <template #completed>
      <span v-text="$t('instances.env.context_refreshed')" />
    </template>
    <template #failed>
      <span v-text="$t('instances.env.context_refresh_failed')" />
    </template>
  </sba-action-button-scoped>

  <sba-modal v-model="isModalOpen" data-testid="refreshModal">
    <template #header>
      <span v-text="$t('instances.env.context_refreshed')" />
    </template>
    <template #body>
      <span v-text="$t('instances.env.refreshed_configurations')" />
      <ul v-if="currentScope === 'instance'" class="properties-list">
        <li
          v-for="(entry, idx) in instanceChangedProperties"
          :key="idx"
          v-text="entry"
        />
      </ul>
      <ul v-else class="properties-list">
        <template
          v-for="(entry, idx) in applicationChangedProperties"
          :key="idx"
        >
          <li v-text="`instanceId: ${entry.instanceId}`" />
          <ul class="properties-list">
            <li
              v-for="(property, pIdx) in entry.changedProperties"
              :key="pIdx"
              v-text="property"
            />
          </ul>
        </template>
      </ul>
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
    instanceChangedProperties() {
      if (this.refreshedProperties.length === 0) {
        return [];
      }
      return this.refreshedProperties[0].changedProperties;
    },
    applicationChangedProperties() {
      return this.refreshedProperties.filter(
        (property) => property.changedProperties.length > 0,
      );
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
