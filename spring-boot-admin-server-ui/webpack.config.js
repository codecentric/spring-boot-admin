'use strict';

var Webpack = require('webpack'),
  NgAnnotatePlugin = require('ng-annotate-webpack-plugin'),
  CopyWebpackPlugin = require('copy-webpack-plugin'),
  CleanWebpackPlugin = require('clean-webpack-plugin'),
  ExtractTextPlugin = require('extract-text-webpack-plugin'),
  path = require('path'),
  glob = require('glob');

var DIST = path.resolve(__dirname, 'target/dist');
var ROOT = __dirname;
var isDevServer = path.basename(require.main.filename) === 'webpack-dev-server.js';

var allModules = glob.sync(path.join(ROOT,'/modules/*/module.js')).map(function (file) {
  var name = /modules\/([^\/]+)\/module\.js/.exec(file)[1];
  return {
    name: name,
    bundle: name + '/module',
    entry: '.' + path.sep + path.relative(ROOT, file),
    outputPath: name + path.sep
  };
});

var getEntries = function (modules) {
  var entries = {
    'core': './core/core.js',
    'dependencies.js': ['jquery', 'bootstrap', 'angular', 'angular-resource', 'angular-ui-router']
  };
  modules.forEach(function (module) {
    entries[module.bundle] = module.entry;
  });
  return entries;
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
    filename: '[name].js'
  },
  resolve: {
    alias: {
      bootstrap: path.resolve(ROOT, 'third-party/bootstrap/js/bootstrap.js'),
      'bootstrap.css': path.resolve(ROOT, 'third-party/bootstrap/css/bootstrap.css'),
      'bootstrap-responsive.css': path.resolve(ROOT, 'third-party/bootstrap/css/bootstrap-responsive.css'),
      'googlefonts.css': path.resolve(ROOT, 'third-party/googlefonts/googlefonts.css'),
      jolokia: path.resolve(ROOT, 'modules/applications-jmx/third-party/jolokia.js')
    }
  },
  module: {
    preLoaders: [{
      test: /\.js$/,
      loader: 'eslint',
      exclude: [/node_modules/, /third-party/]
    }],
    loaders: [
      {
        test: /^jolokia$/,
        loader: 'imports?$=jquery'
      }, {
        test: /\.js$/,
        exclude: [/node_modules/, /third-party/],
        loader: 'ng-annotate'
      }, {
        test: /\.tpl\.html$/,
        loader: 'raw'
      }, {
        test: /\.css(\?.*)?$/,
        loader: ExtractTextPlugin.extract('style', 'css?-minimize')
      }, {
        test: /\.(jpg|png|gif|eot|svg|ttf|woff(2)?)(\?.*)?$/,
        include: /third-party/,
        loader: 'file',
        query: {
          name: '[path][name].[ext]'
		}
      }, {
        test: /\.(jpg|png|gif|eot|svg|ttf|woff(2)?)(\?.*)?$/,
        include: /node_modules/,
        loader: 'file',
        query: {
          name: 'third-party/[path][name].[ext]',
		  context: 'node_modules'
        }
      }, {
        test: /\.(jpg|png|gif)$/,
        include: /core/,
        loader: 'file',
        query: {
          name: '[path][name].[ext]',
          context: 'core'
		}
      }, {
        test: /\.(jpg|png|gif)$/,
        include: /modules/,
        loader: 'file',
        query: {
          name: '[path][name].[ext]',
          context: 'modules'
        }
      }]
  },
  plugins: [
    new CleanWebpackPlugin([DIST]),
    new ExtractTextPlugin('[name].css'),
    new Webpack.ProvidePlugin({
      'window.jQuery': 'jquery'
    }),
    new NgAnnotatePlugin({
      add: true
    }),
    new Webpack.optimize.CommonsChunkPlugin({
      name: 'dependencies.js',
      filename: 'dependencies.js'
    }),
    new CopyWebpackPlugin([{
      from: '**/*.html',
      context: 'core/'
    }, {
      from: '**/*.html',
      context: 'modules/'
    }],
      { ignore: ['*.tpl.html'] })
  ].concat(!isDevServer ? [] : new ModuleConcatPlugin([
    {
      filename: 'all-modules.js',
      test: /module\.js/,
      delimiter: ';\n'
    }, {
      filename: 'all-modules.css',
      test: /module\.css/,
      delimiter: '\n'
    }
  ])),
  devServer: {
    proxy: [{
      context: '/api',
      target: 'http://localhost:8080',
      secure: false
    }]
  },
  node: {
    fs: 'empty'
  }
};
