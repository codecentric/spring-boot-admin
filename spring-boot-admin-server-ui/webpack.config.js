'use strict';

var Webpack = require('webpack'),
  NgAnnotatePlugin = require('ng-annotate-webpack-plugin'),
  CopyWebpackPlugin = require('copy-webpack-plugin'),
  CleanWebpackPlugin = require('clean-webpack-plugin'),
  path = require('path'),
  glob = require('glob');

var DIST = path.resolve(__dirname, 'target/dist');
var ROOT = __dirname;
var isDevServer = path.basename(require.main.filename) === 'webpack-dev-server.js';

var allModules = glob.sync(ROOT + '/modules/*/module.js').map(function (file) {
  var name = /modules\/([^\/]+)\/module\.js/.exec(file)[1];
  return {
    name: name,
    bundle: name + '/module.js',
    entry: file,
    outputPath: path.resolve('./', name)
  };
});

var getEntries = function (modules) {
  var entries = {
    'core.js': './core/core.js',
    'dependencies.js': ['es5-shim/es5-shim', 'es5-shim/es5-sham', 'jquery', 'bootstrap', 'angular', 'angular-resource', 'angular-ui-router']
  };
  modules.forEach(function (module) {
    entries[module.bundle] = module.entry;
  });
  return entries;
};

var getStaticAssets = function (modules) {
  var assets = [{
    from: './core'
    }, {
    from: './dependencies'
    }];
  modules.forEach(function (module) {
    assets.push({
      from: path.dirname(module.entry),
      to: module.outputPath
    });
  });
  return assets;
};


var ConcatSource = require('webpack-sources').ConcatSource;
var ModuleConcatPlugin = function (files) {
  this.files = files;
};
ModuleConcatPlugin.prototype.apply = function (compiler) {
  var self = this;
  compiler.plugin('emit', function (compilation, done) {
    self.files.forEach(function (file) {
      var newSource = new ConcatSource();
      Object.keys(compilation.assets).forEach(function (asset) {
        if (file.test.test(asset)) {
          newSource.add(compilation.assets[asset]);
          newSource.add(file.delimiter);
        }
      });
      if (newSource.children.length > 0) {
        compilation.assets[file.filename] = newSource;
      }
    });
    done();
  });
};

module.exports = {
  context: ROOT,
  entry: getEntries(allModules),
  output: {
    path: DIST,
    filename: '[name]'
  },
  resolve: {
    alias: {
      bootstrap: path.resolve(ROOT, 'dependencies/third-party/bootstrap/bootstrap.js'),
      jolokia: path.resolve(ROOT, 'modules/applications-jmx/third-party/jolokia.js')
    }
  },
  module: {
    preLoaders: [{
      test: /\.js$/,
      loader: 'eslint',
      exclude: [/node_modules/, /third-party/],
        }],
    loaders: [{
      test: /\.js$/,
      exclude: [/node_modules/, /third-party/],
      loader: 'ng-annotate'
        }, {
      test: /\.html$/,
      loader: 'raw'
        }]
  },
  plugins: [
        new Webpack.ProvidePlugin({
      $: 'jquery',
      jQuery: 'jquery',
      'window.jQuery': 'jquery'
    }),
        new NgAnnotatePlugin({
      add: true
    }),
        new Webpack.optimize.CommonsChunkPlugin({
      name: 'dependencies.js',
      filename: 'dependencies.js'
    }),
        new CopyWebpackPlugin(getStaticAssets(allModules), {
      ignore: ['*.js', '*.tpl.html']
    }),
        new CleanWebpackPlugin([DIST])
    ].concat(!isDevServer ? [] : new ModuleConcatPlugin([
    {
      filename: 'all-modules.js',
      test: /module\.js/,
      delimiter: ';\n'
        }, {
      filename: 'css/all-modules.css',
      test: /css\/module\.css/,
      delimiter: '\n'
        }
    ])),
  devServer: {
    proxy: {
      '/api*': {
        target: 'http://localhost:8080',
        secure: false
      }
    }
  },
  node: {
    fs: 'empty'
  }
};
