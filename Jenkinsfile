#!/usr/bin/env groovy

pipeline {
  agent none
	
  options {
    disableConcurrentBuilds()
  }
	
  stages {
    stage('Build') {
      options {
        timeout(time: 10, unit: 'MINUTES')
      }
      input {
        message 'Please provide version'
        parameters {
          string(name: 'VERSION', defaultValue: 'test', description: 'The version of this release')
          booleanParam(name: 'RELEASE', defaultValue: 'false', description: 'Publish this build?')
        }
      }
      agent { 
        label 'wollmux'
      }
      steps {
        sh "./build ${VERSION}"
      }
    }
  }
}
