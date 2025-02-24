<template>
  <table class="w-full table-auto">
    <colgroup>
      <col class="text-gray-500" />
      <col class="py-3" />
    </colgroup>
    <tbody>
      <!-- NAME -->
      <tr v-if="bean.name !== bean.shortName">
        <th class="label" v-text="$t('instances.beans.name')" />
        <td class="value" v-text="bean.name" />
      </tr>

      <!-- ALIAS -->
      <tr v-for="(alias, idx) in bean.aliases" :key="alias">
        <th
          v-if="idx === 0"
          :rowspan="bean.aliases.length"
          class="label"
          v-text="$t('instances.beans.aliases')"
        />
        <td class="value" v-text="alias" />
      </tr>

      <!-- BEAN TYPE -->
      <tr v-if="bean.type !== bean.shortType">
        <th class="label" v-text="$t('instances.beans.type')" />
        <td class="value" v-text="bean.type" />
      </tr>

      <!-- BEAN RESOURCE -->
      <tr v-if="bean.resource">
        <th class="label" v-text="$t('instances.beans.resource')" />
        <td class="value" v-text="bean.resource" />
      </tr>

      <!-- BEAN DEPENDENCIES -->
      <tr v-if="bean.dependencies">
        <th class="label" v-text="$t('instances.beans.dependencies')" />
        <td class="value px-4">
          <ul>
            <li
              v-for="dependency in bean.dependencies"
              :key="dependency"
              class="list-disc break-all"
              v-text="dependency"
            />
          </ul>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<script>
export default {
  name: 'BeansListDetails',
  props: {
    bean: {
      type: Object,
      required: true,
    },
  },
};
</script>

<style>
.label {
  @apply text-gray-500 text-sm text-right px-2 align-top w-40;
}
.value {
  @apply break-all;
}
</style>
