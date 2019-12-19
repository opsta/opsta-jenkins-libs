/**
  * Run Maven Unit Tests
  *
  * @param paramArgs Map of optional variables
  * [
  *   containerName: String         Container name in podTemplate
  *   mavenSettingsFilePath: String Custom Maven settings file path
  *                                 eg. ./.m2/maven-mirror-settings.yml
  *   jUnitReportPath: String       JUnit Output Report Path
  * ]
  */
def call(
  Map paramArgs = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    mavenSettingsFilePath: '',
    containerName: 'maven',
    jUnitReportPath: ''
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  try {
    stage('Maven Unit Tests') {
      container(args.containerName) {

        // Check if need custom maven settings
        mavenSettingsFilePathParameter = ''
        if(!args.mavenSettingsFilePath.isEmpty()) {
          mavenSettingsFilePathParameter = "-s ${args.mavenSettingsFilePath}"
        }

        sh """
          mvn -e ${mavenSettingsFilePathParameter} clean test
        """
      }
    }
  } catch(Exception e) {
    // Print error
    echo e.toString()
    currentBuild.result = 'FAILURE'
  } finally {
    if(!args.jUnitReportPath.isEmpty()) {
        junit args.jUnitReportPath
    }
    // Stop job when failure
    if(currentBuild.result == 'FAILURE') {
      sh "exit 1"
    }
  }
}
