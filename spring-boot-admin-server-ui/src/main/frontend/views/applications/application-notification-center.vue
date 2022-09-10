<template>
  <Popover class="relative">
    <PopoverButton :as="SbaButton" class="mr-1" :disabled="notificationFiltersLength === 0"
                   :title="t('applications.actions.notification_filters')">
      <font-awesome-icon :icon="notificationFiltersLength > 0 ? 'bell-slash' : 'bell'"/>
    </PopoverButton>

    <PopoverPanel
      class="absolute left-1/2 z-10 mt-3 w-screen max-w-xl -translate-x-1/2 transform px-4 sm:px-0 shadow-lg text-sm"
      :as="SbaPanel" v-slot="{ close }">


      <div class="font-semibold leading-5" v-text="t('applications.actions.notification_filters')"/>
      <div class="mt-2 leading-5 text-slate-500" v-text="t('notification_filter_center.description')" />

      <div class="flex items-center border-t border-gray-50"
           :class="{'mt-4': idx === 0, 'py-3': idx < notificationFiltersLength-1, 'pt-3': idx >= notificationFiltersLength-1}"
           v-for="(filter, idx) in notificationFilters">
        <div class="w-1/2">
          {{ filter.instanceId || filter.applicationName }}
        </div>
        <div class="flex-1">
          <strong
            v-text="filter.expiry ? filter.expiry.locale(locale).fromNow(true) : t('term.ever') "
          />
        </div>
        <div class="flex-none text-right text-red-700">
          <button @click.stop="removeFilter(filter, close)" :disabled="executing[filter.id]">
            {{ t('term.delete') }}
          </button>
        </div>
      </div>
    </PopoverPanel>
  </Popover>
</template>

<script setup>
import {Popover, PopoverButton, PopoverPanel} from '@headlessui/vue'
import SbaButton from "../../components/sba-button";
import SbaPanel from "../../components/sba-panel";
import {useI18n} from "vue-i18n";
import {ref, watch} from "vue";

const props = defineProps({
  notificationFilters: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['filter-deleted']);

const i18n = useI18n();
const t = i18n.t;
const locale = i18n.locale.value;
const executing = ref({})

const notificationFiltersLength = ref(0);

watch(() => props.notificationFilters, (notificationFilters) => {
  notificationFiltersLength.value = notificationFilters.length;
});

const removeFilter = async (filter, closePopover) => {
  executing[filter.id] = true;
  await filter.delete();
  emit('filter-deleted', filter.id);
  delete executing[filter];

  if (notificationFiltersLength.value <= 1) {
    closePopover();
  }
}
</script>
