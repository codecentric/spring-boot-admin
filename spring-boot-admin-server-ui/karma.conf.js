'use strict';

var argv = require('yargs').argv;

module.exports = function (config) {
    config.set({
        basePath: '',
        frameworks: ['jasmine'],
        files: ['test/unit/**/*.js'],
        exclude: [],
        preprocessors: {
            'test/unit/**/*.js': ['webpack']
        },
        webpack: {
            module: {
                loaders: [{
                    test: /\.js$/,
                    exclude: [/node_modules/, /third-party/],
                    loader: 'ng-annotate'
                }, {
                    test: /\.html$/,
                    loader: 'raw'
                }]
            }
        },
        plugins: [
            require('karma-jasmine'),
            require('karma-webpack'),
            require('karma-chrome-launcher'),
            require('karma-phantomjs-launcher')
        ],
        reporters: ['progress'],
        port: 9876,
        colors: true,
        logLevel: config.LOG_INFO,
        autoWatch: false,
        browsers: ['PhantomJS'],
        singleRun: true
    });

    if (argv.watch) {
        config.set({
            autoWatch: true,
            browsers: ['Chrome'],
            singleRun: false
        });
    }
};
