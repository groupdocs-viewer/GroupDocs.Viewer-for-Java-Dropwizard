var gulp = require('gulp')

gulp.task('build', function () {
    return gulp
        .src('./node_modules/@groupdocs.examples.angular/**')
        .pipe(gulp.dest('./src/main/resources/assets/angular/'))
});
