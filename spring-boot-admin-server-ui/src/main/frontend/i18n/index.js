import _ from 'lodash';
import langs from 'langs';
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
  .reduce((prev, cur) => _.merge(prev, cur), {});

const i18n = new VueI18n({
  fallbackLocale: 'en',
  locale: 'en',
  messages
});


export function getReadableLanguage(locale) {
  return langs.where('1', locale.toLowerCase().split('-')[0]).local;
}

export const AVAILABLE_LANGUAGES = Object.keys(messages);

export default i18n;
