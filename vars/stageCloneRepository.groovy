/**
  * Git Clone Repository and Checkout
  *
  * @return scmVars Object
  */
def call()
  stage('Clone repository') {
    scmVars = checkout scm
  }
  return scmVars
}
