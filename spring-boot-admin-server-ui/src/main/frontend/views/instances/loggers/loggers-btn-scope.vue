<template>
  <div class="field is-narrow">
    <div class="control">
      <button
        v-if="scope === 'application'"
        class="loggers__toggle-scope button is-primary is-active"
        @click="$emit('changeScope', 'instance')"
      >
        <font-awesome-icon icon="cubes" />&nbsp;
        <span v-text="$t('instances.loggers.application')" />
      </button>
      <button
        v-else
        class="loggers__toggle-scope button"
        @click="$emit('changeScope', 'application')"
      >
        <font-awesome-icon icon="cube" />&nbsp;&nbsp;
        <span v-text="$t('instances.loggers.instance')" />
      </button>
    </div>
    <p class="help has-text-centered">
      <span v-if="scope === 'application'"
            v-text="$t('instances.loggers.affects_all_instances', {count: instanceCount})"
      />
      <span v-else v-text="$t('instances.loggers.affects_this_instance_only')" />
    </p>
  </div>
</template>

<script>
export default {
  props: {
    instanceCount: {
      type: Number,
      required: true
    },
    scope: {
      type: String,
      required: true
    },
  }
}
</script>
