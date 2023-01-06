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
  <NavbarLink
    :view="{ label: selectedLanguage.label }"
    :subitems="languages"
    @menu-item-clicked="localeChanged"
  />
</template>

<script>
import { directive as onClickaway } from 'vue3-click-away';

import NavbarLink from '@/shell/NavbarLink';

export default {
  components: { NavbarLink },
  directives: { onClickaway },
  props: {
    availableLocales: { type: Array, required: true },
    currentLocale: { type: String, required: true },
  },
  emits: ['localeChanged'],
  data() {
    return {
      showLanguages: false,
    };
  },
  computed: {
    selectedLanguage() {
      return this.mapLocale(this.currentLocale);
    },
    languages() {
      return this.availableLocales.map(this.mapLocale);
    },
  },
  methods: {
    localeChanged($event) {
      const selectedLocale = $event.locale;
      if (selectedLocale !== this.currentLocale) {
        this.$emit('localeChanged', selectedLocale);
      }
      this.showLanguages = !this.showLanguages;
    },
    mapLocale(locale) {
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
      } catch (e) {
        return {
          locale,
          label: locale,
        };
      }
    },
  },
};
</script>
