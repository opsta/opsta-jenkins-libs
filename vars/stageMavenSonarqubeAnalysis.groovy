// Run SonarQube Analysis with Maven
def call(
  String sonarQubeEnv,
  String sonarQubeMavenPluginVersion = "3.7.0.1746",
  String settingsFilePath = '',
  String containerName = 'maven'
) {
  stage('SonarQube Analysis with Maven') {
    container(containerName) {
      withSonarQubeEnv(sonarQubeEnv) {

        // Check if need custom maven settings
        settingsFilePathParameter = ''
        if(!settingsFilePath.isEmpty()) {
          settingsFilePathParameter = "-s ${settingsFilePath}"
        }

        sh """
          mvn ${settingsFilePathParameter} -e \
            org.sonarsource.scanner.maven:sonar-maven-plugin:${sonarQubeMavenPluginVersion}:sonar
        """
      }
    }
  }
}
