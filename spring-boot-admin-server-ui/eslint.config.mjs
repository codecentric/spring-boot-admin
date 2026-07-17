// @ts-check
import js from '@eslint/js';
import prettier from 'eslint-plugin-prettier/recommended';
import pluginVue from 'eslint-plugin-vue';
import { defineConfig, globalIgnores } from 'eslint/config';
import globals from 'globals';
import tseslint from 'typescript-eslint';
import vueParser from 'vue-eslint-parser';

export default defineConfig([
  globalIgnores(['**/dist']),

  // Base JS + TS recommended rules
  js.configs.recommended,
  ...tseslint.configs.recommended,

  // Vue recommended (flat config)
  ...pluginVue.configs['flat/recommended'],

  // Prettier integration (must come last to override formatting rules)
  prettier,

  // Project-wide settings
  {
    languageOptions: {
      sourceType: 'module',
      ecmaVersion: 2020,
      globals: {
        ...globals.browser,
        ...globals.node,
        globalThis: false,
        window: false,
      },
      parser: vueParser,
      parserOptions: {
        parser: tseslint.parser,
      },
    },

    rules: {
      'no-console': ['error', { allow: ['warn', 'error'] }],
      'no-undef': 'off', // TypeScript handles this

      'no-restricted-syntax': [
        'error',
        {
          message: 'Please do not commit this.',
          selector: 'MemberExpression > Identifier[name="logTestingPlaygroundURL"]',
        },
      ],

      quotes: [2, 'single', { avoidEscape: true }],

      // Vue rules
      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'off',
      'vue/no-reserved-component-names': 'off',
      'vue/no-v-text-v-html-on-component': 'off',

      // TypeScript rules
      '@typescript-eslint/no-explicit-any': 'off',
    },
  },

  // Test file overrides
  {
    files: ['**/*.spec.{js,jsx,ts,tsx,vue}', '**/*.test.{js,jsx,ts,tsx,vue}'],
    rules: {
      'vue/one-component-per-file': 'off',
    },
  },
]);
