'use strict';

var browserify = require('browserify'),
    clean = require('gulp-clean'),
    connect = require('gulp-connect'),
    eslint = require('gulp-eslint'),
    glob = require('glob'),
    gulp = require('gulp'),
    gutil = require('gulp-util'),
    karma = require('gulp-karma'),
    mocha = require('gulp-mocha'),
    ngAnnotage = require('gulp-ng-annotate'),
    protractor = require('gulp-protractor').protractor,
    concat = require('gulp-concat'),
    source = require('vinyl-source-stream'),
    streamify = require('gulp-streamify'),
    uglify = require('gulp-uglify'),
    shell = require('gulp-shell'),
    rename = require('gulp-rename'),
    argv = require('yargs').argv,
    merge = require('merge-stream');

var backendPort = 8080,
    serverPort = 9000,
    liveReload = true,
    targetDir = './target/';

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
    return gulp.src([ target('dist/')
                    , target('ngAnnotate/')
                    , target('test/')
                    ], {
            read: false
        })
        .pipe(clean());
});

gulp.task('lint', function () {
    return gulp.src([
            'gulpfile.js',
            'core/**/*.js',
            'modules/**/*.js',
            'test/**/*.js',
            '!**/third-party/**',
            '!test/browserified/**'
        ])
        .pipe(eslint())
        .pipe(eslint.format());
});

gulp.task('ngAnnotate', function () {
	return gulp.src([
	                 'core/**/*.js',
	                 'modules/**/*.js',
	                 '!**/third-party/**',
	                 ])
	                 .pipe(ngAnnotage())
	                 .pipe(gulp.dest(target('/ngAnnotate')));
});

gulp.task('unit', skipTests(function () {
    return gulp.src([
            'test/unit/**/*.js'
        ])
        .pipe(mocha({
            reporter: 'dot'
        }));
}));

gulp.task('browserify', ['lint', 'ngAnnotate', 'unit'], function () {
	var dependencies = ['es5-shim/es5-shim', 'es5-shim/es5-sham', 'jquery', 'bootstrap', 'angular',  'angular-resource', 'angular-ui-router'];
	var withExternals = function (bundle) {
	    dependencies.forEach(function(dependency) {
	        bundle.external(dependency);
	    });
	    return bundle;
	};
	var withRequire = function (bundle) {
	    dependencies.forEach(function(dependency) {
	        bundle.require(dependency, { expose : dependency });
	    });
	    return bundle;
	};
	var errorHandler = function(error) {
	      gutil.log(gutil.colors.red(error));
	      this.emit('end');
	};

	var tasks = [];
        tasks.push(withRequire(browserify())
                .bundle()
                .on('error', errorHandler)
                .pipe(source('dependencies.js'))
                .pipe(gulp.dest(target('dist/'))));

	tasks.push(withExternals(browserify(target('ngAnnotate/core.js')))
			.bundle()
			.on('error', errorHandler)
			.pipe(source('core.js'))
			.pipe(gulp.dest(target('dist/'))));

	glob.sync(target('ngAnnotate/*/module.js')).forEach(function (file) {
		tasks.push(withExternals(browserify(file))
		    .bundle()
		    .on('error', errorHandler)
		    .pipe(source('module.js'))
		    .pipe(gulp.dest(file.replace(/\/ngAnnotate\//,'/dist/').replace(/\/module\.js/,''))));  
	});
	
    return merge(tasks).pipe(connect.reload());
});

gulp.task('minify', ['browserify'], function () {
	return gulp.src(target('dist/**/*.js'))
		.pipe(streamify(uglify({ mangle: false  })))
		.pipe(rename({suffix: '.min'}))
		.pipe(gulp.dest(target('dist/')));
});

gulp.task('copy', function () {
    gulp.src(['dependencies/**', 'core/**', 'modules/**', '!**/*.js'] , { nodir: true })
        .pipe(gulp.dest(target('dist/')));
});

gulp.task('browserify-tests', function () {
    var bundler = browserify({ debug: true });
    glob.sync('./test/unit/**/*.js')
        .forEach(function (file) {
            bundler.add(file);
        });
    return bundler
        .bundle()
        .pipe(source('browserified_tests.js'))
        .pipe(gulp.dest(target('test')));
});

gulp.task('karma', ['browserify-tests'], skipTests(function () {
    return gulp.src(target('test/browserified_tests.js'))
    	.pipe(karma({
            configFile: forEnv('karma.conf.js'),
            action: 'run'
        }))
        .on('error', function (err) {
            throw err;
        });
}));

gulp.task('backend-server', shell.task([
    'cd ../spring-boot-admin-samples/spring-boot-admin-sample && mvn spring-boot:run -D"run.arguments=--server.port=' + backendPort + '"'
]));

gulp.task('server', ['concat-module-js', 'copy', 'concat-module-css'], function () {
    if (argv.nobackend === true) {
        process.stdout.write('Running UI only\n');
    } else {
        gulp.start('backend-server');
    }
    connect.server({
        root: target('dist/'),
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
            args: ['--baseUrl', 'http://localhost:' + serverPort]
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
        'core/**',
        'modules/**',
        '!**/third-party/**',
        'test/**/*.js'
    ], ['fast']);
});

gulp.task('concat-module-js', ['browserify'], function() {
	//concatenates all module-js files - this will happen on the server in real life
	return gulp.src(target('dist/*/module.js'))
	    .pipe(concat('all-modules.js', {newLine: ';\n'}))
	    .pipe(gulp.dest(target('dist/')));
});

gulp.task('concat-module-css', function() {
	//concatenates all module-css files - this will happen on the server in real life
	return gulp.src('./modules/*/css/module.css')
		.pipe(concat('all-modules.css'))
		.pipe(gulp.dest(target('dist/css/')));
});

gulp.task('fast', ['clean'], function () {
    gulp.start('concat-module-js', 'copy', 'concat-module-css');
});

gulp.task('default', ['clean'], function () {
    liveReload = false;
    gulp.start('karma', 'copy', 'minify' /*, 'e2e'*/);
});
