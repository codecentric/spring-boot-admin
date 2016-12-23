'use strict';

var NgAnnotatePlugin = require('ng-annotate-webpack-plugin'),
  CopyWebpackPlugin = require('copy-webpack-plugin'),
  CleanWebpackPlugin = require('clean-webpack-plugin'),
  ExtractTextPlugin = require('extract-text-webpack-plugin'),
  path = require('path');

var DIST = path.resolve(__dirname, 'target/dist');
var ROOT = __dirname;

var qPathSep = path.sep === '\\' ? '\\\\' : '/';

module.exports = {
  context: ROOT,
  entry: { 'applications-hystrix': './src/module.js' },
  output: {
    path: DIST,
    filename: '[name]/module.js'
  },
  externals: ['angular'],
  resolve: {
    alias: {
      'hystrix/hystrixCommand': path.resolve(ROOT, 'target/hystrix-dashboard/components/hystrixCommand/hystrixCommand.js'),
      'hystrix/hystrixCommand.css': path.resolve(ROOT, 'target/hystrix-dashboard/components/hystrixCommand/hystrixCommand.css'),
      'hystrix/hystrixThreadPool': path.resolve(ROOT, 'target/hystrix-dashboard/components/hystrixThreadPool/hystrixThreadPool.js'),
      'hystrix/hystrixThreadPool.css': path.resolve(ROOT, 'target/hystrix-dashboard/components/hystrixThreadPool/hystrixThreadPool.css'),
      'tsort': path.resolve(ROOT, 'target/hystrix-dashboard/js/jquery.tinysort.min.js')
    }
  },
  module: {
    preLoaders: [{
      test: /\.js$/,
      loader: 'eslint',
      exclude: [/node_modules/, /hystrix-dashboard/]
    }],
    loaders: [
      {
        test: new RegExp('hystrix-dashboard'+qPathSep+'js'+qPathSep+'jquery\.tinysort\.min\.js$'),
        loader: 'imports?jQuery=jquery'
      }, {
        test: new RegExp('hystrix-dashboard'+qPathSep+'components'+qPathSep+'hystrixCommand'+qPathSep+'hystrixCommand\.js$'),
        loaders: [
          'imports?this=>global&jQuery=jquery&$=jquery&d3&tmpl=microtemplates&tsort',
          'exports?window.HystrixCommandMonitor',
          'regexp-replace?{"match": { "pattern": "\.\./components/hystrixCommand", "flags": "g" }, "replaceWith": "applications-hystrix/components/hystrixCommand"}'
        ]
      }, {
        test: new RegExp('hystrix-dashboard'+qPathSep+'components'+qPathSep+'hystrixThreadPool'+qPathSep+'hystrixThreadPool\.js$'),
        loaders: [
          'imports?this=>global&jQuery=jquery&$=jquery&d3&tmpl=microtemplates&tsort',
          'exports?HystrixThreadPoolMonitor',
          'regexp-replace?{"match": { "pattern": "\.\./components/hystrixThreadPool", "flags": "g" }, "replaceWith": "applications-hystrix/components/hystrixThreadPool"}'
        ]
      }, {
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
      to: 'applications-hystrix',
      context: 'src/'
    }, {
      from: '**/*.html',
      to: 'applications-hystrix/components',
      context: 'target/hystrix-dashboard/components'
    }, {
      from: '**/*.png',
      to: 'applications-hystrix/components',
      context: 'target/hystrix-dashboard/components'
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
          /* Append the applications-hystrix/module.js to the all-modules.js */
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
            require('http').get('http://localhost:9090/applications-hystrix/module.js', function (r) {
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
            require('http').get('http://localhost:9090/applications-hystrix/module.css', function (r) {
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
