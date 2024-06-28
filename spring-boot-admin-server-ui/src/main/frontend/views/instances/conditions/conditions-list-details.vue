<template>
  <table>
    <colgroup>
      <col class="text-gray-500" />
      <col class="py-3" />
    </colgroup>
    <tbody>
      <!-- Condition -->
      <tr v-if="condition.condition">
        <th class="label" v-text="$t('instances.conditions.condition')" />
        <td class="value" v-text="condition.condition" />
      </tr>

      <!-- Message -->
      <tr v-if="condition.message">
        <th class="label" v-text="$t('instances.conditions.message')" />
        <td class="value" v-text="condition.message" />
      </tr>
    </tbody>
  </table>
</template>

<script>
export default {
  name: 'ConditionsListDetails',
  props: {
    condition: {
      type: Object,
      required: true,
    },
  },
};
</script>

<style>
.label {
  @apply text-gray-500 text-sm text-right px-2 align-top w-32;
}
.value {
  @apply text-sm break-all;
}
</style>
