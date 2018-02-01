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

'use strict';
const {join, resolve} = require('path');
const webpack = require('webpack');

const CleanWebpackPlugin = require('clean-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const CommonsChunkPlugin = require('webpack/lib/optimize/CommonsChunkPlugin');
const OptimizeCSSPlugin = require('optimize-css-assets-webpack-plugin');
const LodashModuleReplacementPlugin = require('lodash-webpack-plugin');
const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

const extractCSS = new ExtractTextPlugin({
  filename: 'assets/css/[name].css',
  allChunks: true
});
const extractSASS = new ExtractTextPlugin({
  filename: 'assets/css/[name].css',
  allChunks: true
});

const config = {
  entry: {'sba-core': ['babel-polyfill', './src/main/frontend/index.js']},
  output: {
    path: resolve(__dirname, './target/dist'),
    filename: 'assets/js/[name].js',
    chunkFilename: 'assets/js/[name].js',
    publicPath: ''
  },
  resolve: {
    extensions: ['.js', '.vue'],
    alias: {
      '@': join(__dirname, 'src/main/frontend'),
      'root': join(__dirname, 'node_modules')
      //uncomment to use vue with html compiler:
      // 'vue$': 'vue/dist/vue.esm.js'
    }
  },
  node: {
    fs: 'empty'
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          loaders: {
            js: 'babel-loader',
            scss: ['css-hot-loader'].concat(
              ExtractTextPlugin.extract({
                use: [
                  {loader: 'css-loader'},
                  {loader: 'postcss-loader'},
                  {
                    loader: 'sass-loader', options: {includePaths: [join(__dirname, 'node_modules')]}
                  }],
                fallback: 'style-loader'
              }))
          },
          transformToRequire: {
            img: 'src'
          }
        }
      },
      {
        test: /\.js$/,
        use: 'babel-loader',
        include: [
          resolve(__dirname, 'src/main/frontend'),
          resolve(__dirname, 'node_modules/pretty-bytes')
        ]
      },
      {
        test: /\.css$/,
        use: ['css-hot-loader'].concat(
          ExtractTextPlugin.extract({
            use: ['css-loader', 'postcss-loader'],
            fallback: 'style-loader'
          }))
      },
      {
        test: /\.scss$/,
        use: ['css-hot-loader'].concat(
          ExtractTextPlugin.extract({
            use: [
              {loader: 'css-loader'},
              {loader: 'postcss-loader'},
              {loader: 'sass-loader', options: {includePaths: [join(__dirname, 'node_modules')]}}],
            fallback: 'style-loader'
          }))
      },
      {
        test: /\.html$/,
        use: [{
          loader: 'html-loader',
          options: {
            root: resolve(__dirname, 'src/main/frontend'),
            attrs: ['img:src']
          }
        }]
      },
      {
        test: /\.svg$/,
        loader: 'vue-svg-loader',
      },
      {
        test: /\.(png|jpg|jpeg|gif|eot|ttf|woff|woff2|svgz)(\?.+)?$/,
        use: [{
          loader: 'url-loader',
          options: {
            limit: 1000,
            name: 'assets/img/[name].[ext]'
          }
        }]
      }
    ]
  },
  plugins: [
    new CleanWebpackPlugin(['target/dist'], {
      root: __dirname,
      verbose: true,
      dry: false,
    }),
    new webpack.DefinePlugin({
      '__PROJECT_VERSION__': `'${process.env.PROJECT_VERSION || ''}'`
    }),
    new webpack.NamedModulesPlugin(),
    new LodashModuleReplacementPlugin(),
//    new webpack.ContextReplacementPlugin(/moment[\/\\]locale$/, /en/),
    new webpack.optimize.ModuleConcatenationPlugin(),
    new CommonsChunkPlugin({
      name: 'vendors',
      minChunks: (module) => module.resource && /node_modules/.test(module.resource)
    }),
    extractSASS,
    extractCSS,
    new HtmlWebpackPlugin({
      filename: 'index.html',
      template: './src/main/frontend/index.html',
      inject: false,
      hash: process.env.NODE_ENV === 'production',
    }),
    new HtmlWebpackPlugin({
      filename: 'login.html',
      template: './src/main/frontend/login.html',
      inject: false,
      hash: process.env.NODE_ENV === 'production',
    }),
    new BundleAnalyzerPlugin({
      analyzerMode: 'static',
      openAnalyzer: false,
      reportFilename: '../report.html'
    })
  ],
  devtool: '#eval-source-map'
};

if (process.env.NODE_ENV === 'production') {
  config.devtool = '#source-map';
  // http://vue-loader.vuejs.org/en/workflow/production.html
  config.plugins = config.plugins.concat([
    new webpack.DefinePlugin({
      'process.env': {
        NODE_ENV: '"production"'
      }
    }),
    new webpack.optimize.UglifyJsPlugin({
      compress: {
        warnings: false
      },
      sourceMap: true
    }),
    new OptimizeCSSPlugin()
  ])
}

module.exports = config;