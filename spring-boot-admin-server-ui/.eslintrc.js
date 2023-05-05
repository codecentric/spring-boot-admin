module.exports = {
  root: true,
  env: {
    node: true,
  },
  parser: 'vue-eslint-parser',
  parserOptions: {
    parser: '@typescript-eslint/parser',
    ecmaVersion: 2020,
    sourceType: 'module',
  },
  extends: [
    'plugin:@typescript-eslint/recommended',
    'plugin:vue/vue3-recommended',
    'plugin:prettier/recommended',
  ],
  plugins: ['prettier', '@typescript-eslint'],
  ignorePatterns: ['dist'],
  rules: {
    'vue/multi-word-component-names': 'off',
    'vue/no-v-html': 'off',
    'vue/no-reserved-component-names': 'off',
    'vue/no-v-text-v-html-on-component': 'off',
    quotes: [2, 'single', { avoidEscape: true }],
    'no-trailing-spaces': [2, { skipBlankLines: true }],
  },
  globals: {
    globalThis: false,
    window: false,
  },
  overrides: [
    {
      files: ['*.ts'],
      rules: {
        'no-undef': 'off',
      },
    },
  ],
};
