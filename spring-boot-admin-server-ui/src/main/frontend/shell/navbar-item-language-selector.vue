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
  <div class="navbar-item has-dropdown is-hoverable">
    <a class="navbar-link" role="button">
      {{ selectedLanguage.label }} ({{ selectedLanguage.locale }})
    </a>
    <div class="navbar-dropdown">
      <a class="navbar-item"
         role="button"
         v-for="language in languages"
         :key="language.locale"
         @click="localeChanged(language.locale)"
      >
        {{ language.label }} ({{ language.locale }})
      </a>
    </div>
  </div>
</template>

<script>

export default {
  props: {
    availableLocales: {type: Array, required: true},
    currentLocale: {type: String, required: true}
  },
  computed: {
    selectedLanguage() {
      return this.mapLocale(this.currentLocale);
    },
    languages() {
      return this.availableLocales.map(this.mapLocale);
    }
  },
  methods: {
    localeChanged(selectedLocale) {
      if (selectedLocale !== this.currentLocale) {
        this.$emit('localeChanged', selectedLocale)
      }
    },
    mapLocale(locale) {
      try {
        const localeUppercase = locale.split('-').pop().toUpperCase();
        let label = new Intl.DisplayNames([localeUppercase, this.$i18n.locale], {type: 'region'}).of(localeUppercase);

        if (label?.toUpperCase() === 'UNKNOWN REGION') {
          label = locale;
        }

        return {
          locale,
          label
        };
      } catch (e) {
        return {
          locale,
          label: locale
        };
      }
    }
  }
}
</script>

<style scoped>

</style>
