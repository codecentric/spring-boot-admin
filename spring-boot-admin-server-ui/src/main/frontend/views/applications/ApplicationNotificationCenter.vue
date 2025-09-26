<template>
  <sba-button
    :disabled="notificationFiltersLength === 0"
    :title="
      notificationFiltersLength > 0
        ? t('applications.actions.notification_filters')
        : t('applications.notification_filter.none')
    "
    class="mr-1"
    @click="togglePopover"
  >
    <font-awesome-icon
      :icon="notificationFiltersLength > 0 ? 'bell-slash' : 'bell'"
    />
  </sba-button>

  <Popover ref="popoverRef">
    <div
      class="font-semibold leading-5"
      v-text="t('applications.actions.notification_filters')"
    />
    <div
      class="mt-2 leading-5 text-slate-500"
      v-text="t('notification_filter_center.description')"
    />
    <div
      v-for="(filter, idx) in notificationFilters"
      :key="filter.id"
      :class="{
        'mt-4': idx === 0,
        'py-3': idx < notificationFiltersLength - 1,
        'pt-3': idx >= notificationFiltersLength - 1,
      }"
      class="flex items-center border-t border-gray-50"
    >
      <div class="w-1/2">
        {{ filter.instanceId || filter.applicationName }}
      </div>
      <div class="flex-1">
        <strong
          v-text="
            filter.expiry
              ? filter.expiry.locale(momentLocale).fromNow(true)
              : t('term.ever')
          "
        />
      </div>
      <div class="flex-none text-right text-red-700">
        <button
          :disabled="executing[filter.id]"
          @click.stop="removeFilter(filter)"
        >
          {{ t('term.delete') }}
        </button>
      </div>
    </div>
  </Popover>
</template>

<script setup>
import { Popover } from 'primevue';
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

import SbaButton from '@/components/sba-button';

const props = defineProps({
  notificationFilters: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits(['filter-remove']);

const i18n = useI18n();
const t = i18n.t;
const locale = i18n.locale;
const executing = ref({});
const popoverRef = ref();

const notificationFiltersLength = ref(0);

const momentLocale = computed({
  get() {
    return locale.value;
  },
});

watch(
  () => props.notificationFilters,
  (notificationFilters) => {
    notificationFiltersLength.value = notificationFilters.length;
  },
);

const togglePopover = (event) => {
  popoverRef.value.toggle(event);
};

const removeFilter = async (filter) => {
  executing[filter.id] = true;
  emit('filter-remove', filter);

  if (notificationFiltersLength.value <= 1) {
    popoverRef.value.hide();
  }
};
</script>
