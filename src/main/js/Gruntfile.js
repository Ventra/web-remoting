odule.exports = function(grunt) {
  grunt.initConfig({
    "default": {
      bower_concat: {
        all: {
          dest: 'target.js'
        }
      }
    }
  });


  grunt.loadNpmTasks('grunt-bower-concat');
  grunt.registerTask('default');
};