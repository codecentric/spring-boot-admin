'use strict';

var browserify = require('browserify'),
    clean = require('gulp-clean'),
    connect = require('gulp-connect'),
    eslint = require('gulp-eslint'),
    glob = require('glob'),
    gulp = require('gulp'),
    karma = require('gulp-karma'),
    mocha = require('gulp-mocha'),
    ngAnnotage = require('gulp-ng-annotate'),
    protractor = require('gulp-protractor').protractor,
    source = require('vinyl-source-stream'),
    streamify = require('gulp-streamify'),
    uglify = require('gulp-uglify'),
    shell = require('gulp-shell'),
    argv = require('yargs').argv;

var backendPort = 8080,
    serverPort = 9000,
    liveReload = true,
    targetDir = './target';

/*
 * Useful tasks:
 * - gulp fast:
 *   - linting
 *   - unit tests
 *   - browserification
 *   - no minification, does not start server.
 * - gulp watch:
 *   - starts server with live reload enabled
 *   - lints, unit tests, browserifies and live-reloads changes in browser
 *   - no minification
 * - gulp:
 *   - linting
 *   - unit tests
 *   - browserification
 *   - minification and browserification of minified sources
 *   - start server for e2e tests
 *   - run Protractor End-to-end tests
 *   - stop server immediately when e2e tests have finished
 *
 * At development time, you should usually just have 'gulp watch' running in the
 * background all the time. Use 'gulp' before releases.
 */

function target(dir) {
    return targetDir + dir;
}

function forEnv(f) {
    return process.env.TRAVIS === 'true' ? f + '.travis' : f;
}

function skipTests(task) {
    if (argv.skipTests === 'true') {
        return function () {
            process.stdout.write('Skipped Tests...\n');
        };
    } else {
        return task;
    }
}

gulp.task('clean', function () {
    return gulp.src([ target('/dist')
                    , target('/ngAnnotate')
                    , target('/test/')
                    ], {
            read: false
        })
        .pipe(clean());
});

gulp.task('lint', function () {
    return gulp.src([
            'gulpfile.js',
            'app/js/**/*.js',
            'test/**/*.js',
            '!app/js/third-party/**',
            '!test/browserified/**',
        ])
        .pipe(eslint())
        .pipe(eslint.format());
});

gulp.task('unit', skipTests(function () {
    return gulp.src([
            'test/unit/**/*.js'
        ])
        .pipe(mocha({
            reporter: 'dot'
        }));
}));

gulp.task('browserify', ['lint', 'unit'], function () {
    return browserify('./app/js/app.js')
        .bundle({
            debug: true
        })
        .pipe(source('app.js'))
        .pipe(gulp.dest(target('/dist/js')))
        .pipe(connect.reload());
});

gulp.task('ngAnnotate', ['lint', 'unit'], function () {
    return gulp.src([
            'app/js/**/*.js',
            '!app/js/third-party/**',
        ])
        .pipe(ngAnnotage())
        .pipe(gulp.dest(target('/ngAnnotate')));
});

gulp.task('browserify-min', ['ngAnnotate'], function () {
    return browserify(target('/ngAnnotate/app.js'))
        .bundle()
        .pipe(source('app.min.js'))
        .pipe(streamify(uglify({
            mangle: false
        })))
        .pipe(gulp.dest(target('/dist/js')));
});

gulp.task('browserify-tests', function () {
    var bundler = browserify();
    glob.sync('./test/unit/**/*.js')
        .forEach(function (file) {
            bundler.add(file);
        });
    return bundler
        .bundle({
            debug: true
        })
        .pipe(source('browserified_tests.js'))
        .pipe(gulp.dest(target('/test/browserified')));
});

gulp.task('karma', ['browserify-tests'], skipTests(function () {
    return gulp
        .src(target('/test/browserified/browserified_tests.js'))
        .pipe(karma({
            configFile: forEnv('karma.conf.js'),
            action: 'run'
        }))
        .on('error', function (err) {
            // Make sure failed tests cause gulp to exit non-zero
            throw err;
        });
}));

gulp.task('copy', function () {
    gulp.src(['app/**', '!**/*.js'])
        .pipe(gulp.dest(target('/dist')));
});

gulp.task('backend-server', shell.task([
    'cd ../spring-boot-admin-samples/spring-boot-admin-sample && mvn spring-boot:run -D"run.arguments=--server.port=' +
    backendPort + '"'
]));

gulp.task('server', ['browserify', 'copy'], function () {
    gulp.start('backend-server');
    connect.server({
        root: target('/dist'),
        livereload: liveReload,
        port: serverPort,
        middleware: function () {
            return [(function () {
                var url = require('url');
                var proxy = require('proxy-middleware');
                var options = url.parse('http://localhost:' +
                    backendPort + '/api');
                options.route = '/api';
                return proxy(options);
            })()];
        }
    });
});

gulp.task('e2e', ['server'], skipTests(function () {
    return gulp.src(['./test/e2e/**/*.js'])
        .pipe(protractor({
            configFile: forEnv('protractor.conf.js'),
            args: ['--baseUrl', 'http://localhost:' + serverPort],
        }))
        .on('error', function (e) {
            throw e;
        })
        .on('end', function () {
            connect.serverClose();
        });
}));

gulp.task('watch', function () {
    gulp.start('server');
    gulp.watch([
        'app/**',
        '!app/js/third-party/**',
        'test/**/*.js',
    ], ['fast']);
});

gulp.task('fast', ['clean'], function () {
    gulp.start('browserify', 'copy');
});

gulp.task('default', ['clean'], function () {
    liveReload = false;
    gulp.start('karma', 'browserify', 'copy', 'browserify-min' /*, 'e2e'*/);
});
