/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

module.exports = {
  outputDir: 'target/dist',
  chainWebpack: config => {
    config.entryPoints.delete('app');
    config.entry('custom').add('./src/index.js');
    config.externals({
      vue: {
        commonjs: 'vue',
        commonjs2: 'vue',
        root: 'Vue'
      }
    });
    config.output.libraryTarget('var');
    config.optimization.splitChunks(false);
    config.module
      .rule('vue')
      .use('vue-loader')
      .loader('vue-loader')
      .tap(options => ({
        ...options,
        hotReload: false
      }));
    config.plugins.delete('html');
    config.plugins.delete('preload');
    config.plugins.delete('prefetch');
  }
};
