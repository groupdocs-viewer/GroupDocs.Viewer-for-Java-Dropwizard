var gulp = require('gulp')

gulp.task('build', function() {
  return gulp
    .src('./node_modules/@groupdocs.examples.jquery/**')
    .pipe(gulp.dest('./src/main/resources/assets/'))
});

gulp.task('copy', function() {
  return gulp
      .src('./node_modules/@groupdocs.examples.angular/viewer/dist/**')
      .pipe(gulp.dest('./src/main/resources/assets/static/'))
});

gulp.task('config', function() {
  return gulp
      .src('./node_modules/@groupdocs.examples.angular/viewer/dist/assets/**')
      .pipe(gulp.dest('./src/main/resources/assets/'))
});