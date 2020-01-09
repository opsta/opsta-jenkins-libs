/**
  * Run SonarQube Quality Gate
  *
  * @param paramArgs Map of optional variables
  * [
  *   containerName: String     Container name in podTemplate
  *   sonarQubeTimeout: Integer Number of timeout depends on sonarQubeUnit
  *   sonarQubeUnit: String     Unit for timeout
  *                             choices. MINUTES, HOURS, DAYS, MONTHS, YEARS
  * ]
  */
def call(
  Map paramArgs = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    sonarQubeTimeout: 1,
    sonarQubeUnit: 'HOURS',
    containerName: 'maven'
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  stage("SonarQube Quality Gate") {
    container(args.containerName) {
      // Just in case something goes wrong, pipeline will be killed after a timeout
      timeout(time: args.sonarQubeTimeout, unit: args.sonarQubeUnit) {
        def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
        if (qg.status != 'OK') {
          error "Pipeline aborted due to quality gate failure: ${qg.status}"
        }
      }
    }
  }
}
