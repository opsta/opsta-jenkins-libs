/**
  * Git Clone Repository and Checkout
  *
  * @param branch String of branch name to checkout
  *               eg. dev, refs/tags/v1.0, refs/heads/feature1
  *
  * @return scmVars Object
  */
def call(branch = '') {

  stage('Clone repository') {

    if(branch == '') {
      scmVars = checkout scm
    } else {

      // Checkout Branch
      checkout scm
      scmVars = checkout scm: [
        $class: 'GitSCM',
        branches: [[name: branch]]
      ]

      // Need to assign GIT_COMMIT because to this bug https://issues.jenkins-ci.org/browse/JENKINS-45489
      scmVars['GIT_COMMIT'] = sh(returnStdout: true, script: "git rev-parse HEAD").trim()

    }

  }

  return scmVars

}
