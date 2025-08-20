import { FlatCompat } from '@eslint/eslintrc';
import js from '@eslint/js';
import typescriptEslint from '@typescript-eslint/eslint-plugin';
import prettier from 'eslint-plugin-prettier';
import pluginVue from 'eslint-plugin-vue';
import { defineConfig, globalIgnores } from 'eslint/config';
import globals from 'globals';
import parser from 'vue-eslint-parser';

const compat = new FlatCompat({
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all,
});

export default defineConfig([
  ...pluginVue.configs['flat/recommended'],
  {
    languageOptions: {
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.node,
        globalThis: false,
        window: false,
      },

      parser: parser,
      ecmaVersion: 2020,

      parserOptions: {
        parser: '@typescript-eslint/parser',
      },
    },

    extends: compat.extends(
      'plugin:@typescript-eslint/recommended',
      'plugin:prettier/recommended',
    ),

    plugins: {
      prettier,
      '@typescript-eslint': typescriptEslint,
    },

    rules: {
      'no-console': [
        'error',
        {
          allow: ['warn', 'error'],
        },
      ],

      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'off',
      'vue/no-reserved-component-names': 'off',
      'vue/no-v-text-v-html-on-component': 'off',

      quotes: [
        2,
        'single',
        {
          avoidEscape: true,
        },
      ],
    },
  },
  globalIgnores(['**/dist']),
  {
    rules: {
      'no-undef': 'off',
      '@typescript-eslint/no-explicit-any': 'off',
    },
  },
]);
