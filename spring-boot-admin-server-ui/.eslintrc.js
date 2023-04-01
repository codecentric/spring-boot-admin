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
    'eslint:recommended',
    'prettier',
    'plugin:prettier/recommended',
    'plugin:vue/vue3-recommended',
  ],
  plugins: ['prettier', '@typescript-eslint'],
  ignorePatterns: ['dist'],
  rules: {
    'vue/multi-word-component-names': 'off',
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
