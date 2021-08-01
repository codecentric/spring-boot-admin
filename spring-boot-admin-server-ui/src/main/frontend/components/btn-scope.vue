<template>
  <div class="field is-narrow">
    <div class="control">
      <button
        v-if="scope === 'application'"
        class="button is-primary is-active"
        @click="$emit('changeScope', 'instance')"
      >
        <font-awesome-icon icon="cubes" />&nbsp;
        <span v-text="$t('term.application')" />
      </button>
      <button
        v-else
        class="button"
        @click="$emit('changeScope', 'application')"
      >
        <font-awesome-icon icon="cube" />&nbsp;&nbsp;
        <span v-text="$t('term.instance')" />
      </button>
    </div>
    <p class="help has-text-centered">
      <span v-if="scope === 'application'"
            v-text="$t('term.affects_all_instances', {count: instanceCount})"
      />
      <span v-else v-text="$t('term.affects_this_instance_only')" />
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

<style scoped>
.button {
    width: 100%;
}
</style>
