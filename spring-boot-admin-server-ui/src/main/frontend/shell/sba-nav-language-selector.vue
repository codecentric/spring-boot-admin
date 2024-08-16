<!--
  - Copyright 2014-2018 the original author or authors.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
  <sba-nav-dropdown :text="selectedLanguage.label">
    <sba-dropdown-item
      v-for="lang in languages"
      :key="lang.locale"
      @click="() => localeChanged(lang)"
    >
      {{ lang.label }}
    </sba-dropdown-item>
  </sba-nav-dropdown>
</template>

<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

import SbaDropdownItem from '@/components/sba-dropdown/sba-dropdown-item.vue';
import SbaNavDropdown from '@/components/sba-nav/sba-nav-dropdown.vue';

const { locale: currentLocale } = useI18n();

const props = defineProps({
  availableLocales: { type: Array, required: true },
});

const emit = defineEmits(['locale-changed']);

const selectedLanguage = computed(() => {
  return mapLocale(currentLocale.value);
});

const languages = computed(() => {
  return props.availableLocales.map(mapLocale).filter((mappedLocale) => {
    return mappedLocale.locale !== selectedLanguage.value.locale;
  });
});

const localeChanged = ($event) => {
  const selectedLocale = $event.locale;
  if (selectedLocale !== currentLocale) {
    emit('locale-changed', selectedLocale);
  }
};

const mapLocale = (locale) => {
  try {
    let languageTag = locale.split('-').reverse().pop();
    let regionTag =
      locale.split('-').length > 1 ? `-${locale.split('-').pop()}` : '';

    if (locale.toLowerCase().startsWith('zh')) {
      if (locale.endsWith('CN')) {
        regionTag = '-Hans';
      }
      if (locale.endsWith('TW')) {
        regionTag = '-Hant';
      }
    }

    let translatedLanguageNames = new Intl.DisplayNames([locale], {
      type: 'language',
    });
    let label = translatedLanguageNames.of(`${languageTag}${regionTag}`);

    if (label?.toUpperCase() === 'UNKNOWN REGION') {
      label = locale;
    }

    return {
      locale,
      label,
    };
  } catch {
    return {
      locale,
      label: locale,
    };
  }
};
</script>
