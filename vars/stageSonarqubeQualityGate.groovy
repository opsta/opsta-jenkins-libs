// Run SonarQube Quality Gate
def call(
  Integer sonarQubeTimeout = 1,
  String sonarQubeUnit = 'HOURS',
  String containerName = 'maven'
) {
  stage("SonarQube Quality Gate") {
    container(containerName) {
      // Just in case something goes wrong, pipeline will be killed after a timeout
      timeout(time: sonarQubeTimeout, unit: sonarQubeUnit) {
        def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
        if (qg.status != 'OK') {
          error "Pipeline aborted due to quality gate failure: ${qg.status}"
        }
      }
    }
  }
}
