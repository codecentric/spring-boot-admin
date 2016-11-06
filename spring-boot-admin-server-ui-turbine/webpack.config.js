'use strict';

var NgAnnotatePlugin = require('ng-annotate-webpack-plugin'),
  CopyWebpackPlugin = require('copy-webpack-plugin'),
  CleanWebpackPlugin = require('clean-webpack-plugin'),
  ExtractTextPlugin = require('extract-text-webpack-plugin'),
  path = require('path');

var DIST = path.resolve(__dirname, 'target/dist');
var ROOT = __dirname;

module.exports = {
  context: ROOT,
  entry: { 'turbine': './src/main/webpack/module.js' },
  output: {
    path: DIST,
    filename: '[name]/module.js'
  },
  externals: ['angular'],
  module: {
    preLoaders: [{
      test: /\.js$/,
      loader: 'eslint',
      exclude: [/node_modules/]
    }],
    loaders: [
      {
        test: /\.js$/,
        exclude: [/node_modules/],
        loader: 'ng-annotate'
      }, {
        test: /\.tpl\.html$/,
        loader: 'raw'
      }, {
        test: /\.css(\?.*)?$/,
        loader: ExtractTextPlugin.extract('style', 'css?-minimize')
      }
    ]
  },
  plugins: [
    new CleanWebpackPlugin([DIST]),
    new ExtractTextPlugin('[name]/module.css'),
    new NgAnnotatePlugin({ add: true }),
    new CopyWebpackPlugin([{
      from: '**/*.html',
      to: 'turbine',
      context: 'src/main/webpack'
    }
    ], { ignore: ['*.tpl.html'] })
  ],
  devServer: {
    proxy: [
      {
        context: '/',
        target: 'http://localhost:8080',
        secure: false,
        onProxyRes: function (proxyRes, req, res) {
          /* Append the turbine/module.js to the all-modules.js */
          if (req.path === '/all-modules.js') {
            delete proxyRes.headers['content-length'];
            proxyRes.headers['transfer-encoding'] = 'chunked';
            proxyRes.__pipe = proxyRes.pipe;
            proxyRes.pipe = function (sink, options) {
              var opts = options || {};
              opts.end = false;
              proxyRes.__pipe(sink, opts);
            };
            var suffixModule = '\n';
            require('http').get('http://localhost:9090/turbine/module.js', function (r) {
              r.on('data', function (chunk) {
                suffixModule += chunk;
              });
              r.on('end', function () {
                res.end(suffixModule);
              });
            });
          }

          if (req.path === '/all-modules.css') {
            delete proxyRes.headers['content-length'];
            proxyRes.headers['transfer-encoding'] = 'chunked';
            proxyRes.__pipe = proxyRes.pipe;
            proxyRes.pipe = function (sink, options) {
              var opts = options || {};
              opts.end = false;
              proxyRes.__pipe(sink, opts);
            };
            var suffixCss = '\n';
            require('http').get('http://localhost:9090/turbine/module.css', function (r) {
              r.on('data', function (chunk) {
                suffixCss += chunk;
              });
              r.on('end', function () {
                res.end(suffixCss);
              });
            });
          }
        }
      }
    ]
  },
  node: {
    fs: 'empty'
  }
};
