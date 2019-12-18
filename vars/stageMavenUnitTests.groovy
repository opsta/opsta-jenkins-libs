/**
  * Run Maven Unit Tests
  *
  * @param args Map of optional variables
  * [
  *   containerName: String         Container name in podTemplate
  *   mavenSettingsFilePath: String Custom Maven settings file path
  *                                 eg. ./.m2/maven-mirror-settings.yml
  *   jUnitReportPath: String       JUnit Output Report Path
  * ]
  */
def call(
  Map args = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    settingsFilePath = '',
    containerName = 'maven',
    jUnitReportPath = ''
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << args

  try {
    stage('Maven Unit Tests') {
      container(args.containerName) {

        // Check if need custom maven settings
        settingsFilePathParameter = ''
        if(!args.settingsFilePath.isEmpty()) {
          settingsFilePathParameter = "-s ${args.settingsFilePath}"
        }

        sh """
          mvn -e ${settingsFilePathParameter} clean test
        """
      }
    }
  } catch(Exception e) {
    currentBuild.result = 'FAILURE'
  } finally {
    if(!args.jUnitReportPath.isEmpty()) {
        junit ${args.jUnitReportPath}
    }
    // Stop job when failure
    if(currentBuild.result == 'FAILURE') {
      sh "exit 1"
    }
  }
}
