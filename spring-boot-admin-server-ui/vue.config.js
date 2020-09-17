/*
 * Copyright 2014-2019 the original author or authors.
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

const {resolve} = require('path');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;
const CopyPlugin = require('copy-webpack-plugin');

module.exports = {
  publicPath: './',
  outputDir: 'target/dist',
  assetsDir: 'assets',
  pages: {
    'sba-core': {
      entry: './src/main/frontend/index.js',
      template: './src/main/frontend/index.html',
      filename: 'index.html'
    },
    'login': {
      entry: './src/main/frontend/login.js',
      template: './src/main/frontend/login.html',
      filename: 'login.html'
    }
  },
  chainWebpack: config => {
    if (process.env.NODE_ENV === 'development') {
      //Fix different paths for watch-mode
      config.output.filename('assets/js/[name].js');
      config.output.chunkFilename('assets/js/[name].js');
    }
    config.resolve.alias.set('@', resolve(__dirname, 'src/main/frontend'));
    config.module.rule('html')
      .test(/\.html$/)
      .use('html-loader')
      .loader('html-loader')
      .options({
        attributes: {
          urlFilter: (attribute, value) => value !== 'sba-settings.js',
          root: resolve(__dirname, 'src/main/frontend')
        }
      })
      .end();
    config.plugin('prefetch-sba-core')
      .tap(args => {
        args[0].fileBlacklist = [/\.map$/, /event-source-polyfill\.?[a-z0-9]*\.js/];
        return args;
      });
    config.plugin('preload-login')
      .tap(args => {
        args[0].include.entries = [];
        return args;
      });
    config.plugin('prefetch-login')
      .tap(args => {
        args[0].include.entries = [];
        return args;
      });
    config.plugin('define')
      .tap(args => {
        args[0]['__PROJECT_VERSION__'] = `'${process.env.PROJECT_VERSION || ''}'`;
        return args;
      });
  },
  configureWebpack: {
    plugins: [
      new CopyPlugin([{
        from: resolve(__dirname, 'src/main/frontend/assets'),
        to: resolve(__dirname, 'target/dist/assets'),
        toType: 'dir',
        ignore: ['*.scss']
      }]),
      new CopyPlugin([{
        from: resolve(__dirname, 'src/main/frontend/sba-settings.js'),
        to: resolve(__dirname, 'target/dist/sba-settings.js'),
      }]),
      new BundleAnalyzerPlugin({
        analyzerMode: 'static',
        openAnalyzer: false,
        reportFilename: '../report.html'
      })
    ]
  }
};
