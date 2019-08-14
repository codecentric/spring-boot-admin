import merge from 'lodash/merge';
import Vue from 'vue';
import VueI18n from 'vue-i18n';

Vue.use(VueI18n);

const context = require.context('../', true, /^\.\/*\/.*i18n\.?([^/]*)\.json$/);
const messages = context.keys()
  .map(key => {
    const localeFromFile = /^\.\/*\/.*i18n\.?([^/]*)\.json$/.exec(key);
    const messages = context(key);
    if (localeFromFile[1]) {
      return {
        [localeFromFile[1]]: messages
      }
    } else {
      return messages;
    }
  })
  .reduce((prev, cur) => merge(prev, cur), {});

export const AVAILABLE_LANGUAGES = Object.keys(messages);

const browserLanguage = navigator.language.split('-')[0];

const i18n = new VueI18n({
  fallbackLocale: 'en',
  locale:  AVAILABLE_LANGUAGES.includes(browserLanguage) ? browserLanguage : 'en',
  silentFallbackWarn: process.env.NODE_ENV === 'production',
  silentTranslationWarn: process.env.NODE_ENV === 'production',
  messages
});

export default i18n;
