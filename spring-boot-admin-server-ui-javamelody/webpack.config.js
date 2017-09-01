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
  entry: { 'applications-javamelody': './src/module.js' },
  output: {
    path: DIST,
    filename: '[name]/module.js'
  },
  externals: ['angular'],
  resolve: {
    alias: {
      'javamelody/javamelodyCommand': path.resolve(ROOT, 'target/javamelody-dashboard/components/javamelodyCommand/javamelodyCommand.js'),
      'javamelody/javamelodyCommand.css': path.resolve(ROOT, 'target/javamelody-dashboard/components/javamelodyCommand/javamelodyCommand.css'),
      'javamelody/javamelodyThreadPool': path.resolve(ROOT, 'target/javamelody-dashboard/components/javamelodyThreadPool/javamelodyThreadPool.js'),
      'javamelody/javamelodyThreadPool.css': path.resolve(ROOT, 'target/javamelody-dashboard/components/javamelodyThreadPool/javamelodyThreadPool.css'),
      'tsort': path.resolve(ROOT, 'target/javamelody-dashboard/js/jquery.tinysort.min.js')
    }
  },
  module: {
    preLoaders: [{
      test: /\.js$/,
      loader: 'eslint',
      exclude: [/node_modules/, /javamelody-dashboard/]
    }],
    loaders: [
      {
        test: new RegExp('javamelody-dashboard'+qPathSep+'js'+qPathSep+'jquery\.tinysort\.min\.js$'),
        loader: 'imports?jQuery=jquery'
      }, {
        test: new RegExp('javamelody-dashboard'+qPathSep+'components'+qPathSep+'javamelodyCommand'+qPathSep+'javamelodyCommand\.js$'),
        loaders: [
          'imports?this=>global&jQuery=jquery&$=jquery&d3&tmpl=microtemplates&tsort',
          'exports?window.javamelodyCommandMonitor',
          'regexp-replace?{"match": { "pattern": "\.\./components/javamelodyCommand", "flags": "g" }, "replaceWith": "applications-javamelody/components/javamelodyCommand"}'
        ]
      }, {
        test: new RegExp('javamelody-dashboard'+qPathSep+'components'+qPathSep+'javamelodyThreadPool'+qPathSep+'javamelodyThreadPool\.js$'),
        loaders: [
          'imports?this=>global&jQuery=jquery&$=jquery&d3&tmpl=microtemplates&tsort',
          'exports?javamelodyThreadPoolMonitor',
          'regexp-replace?{"match": { "pattern": "\.\./components/javamelodyThreadPool", "flags": "g" }, "replaceWith": "applications-javamelody/components/javamelodyThreadPool"}'
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
      to: 'applications-javamelody',
      context: 'src/'
    }, {
      from: '**/*.html',
      to: 'applications-javamelody/components',
      context: 'target/javamelody-dashboard/components'
    }, {
      from: '**/*.png',
      to: 'applications-javamelody/components',
      context: 'target/javamelody-dashboard/components'
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
          /* Append the applications-javamelody/module.js to the all-modules.js */
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
            require('http').get('http://localhost:9090/applications-javamelody/module.js', function (r) {
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
            require('http').get('http://localhost:9090/applications-javamelody/module.css', function (r) {
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
