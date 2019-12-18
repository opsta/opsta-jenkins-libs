// Run Maven Unit Tests
def call(
  String settingsFilePath = '',
  String containerName = 'maven',
  String jUnitReportPath = ''
) {
  try {
    stage('Maven Unit Tests') {
      container(containerName) {

        // Check if need custom maven settings
        settingsFilePathParameter = ''
        if(!settingsFilePath.isEmpty()) {
          settingsFilePathParameter = "-s ${settingsFilePath}"
        }

        sh """
          mvn -e ${settingsFilePathParameter} clean test
        """
      }
    }
  } catch(Exception e) {
    currentBuild.result = 'FAILURE'
  } finally {
    if(!jUnitReportPath.isEmpty()) {
        junit ${jUnitReportPath}
    }
    // Stop job when failure
    if(currentBuild.result == 'FAILURE') {
      sh "exit 1"
    }
  }
}
