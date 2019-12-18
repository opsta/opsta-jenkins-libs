/**
  * Run SonarQube Analysis with Maven
  *
  * @param sonarQubeEnv String of SonarQube Environment Name in Jenkins
  * @param args Map of optional variables
  * [
  *   containerName: String               Container name in podTemplate
  *   mavenSettingsFilePath: String       Custom Maven settings file path
  *                                       eg. ./.m2/maven-mirror-settings.yml
  *   sonarQubeMavenPluginVersion: String SonarQube Maven Plugin Version. Pick version from here
  *                                       https://mvnrepository.com/artifact/org.sonarsource.scanner.maven/sonar-maven-plugin
  * ]
  */
def call(
  String sonarQubeEnv,
  Map args = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    sonarQubeMavenPluginVersion: "3.7.0.1746",
    settingsFilePath: '',
    containerName: 'maven'
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << args

  stage('SonarQube Analysis with Maven') {
    container(args.containerName) {
      withSonarQubeEnv(sonarQubeEnv) {

        // Check if need custom maven settings
        settingsFilePathParameter = ''
        if(!args.settingsFilePath.isEmpty()) {
          settingsFilePathParameter = "-s ${args.settingsFilePath}"
        }

        sh """
          mvn ${settingsFilePathParameter} -e \
            org.sonarsource.scanner.maven:sonar-maven-plugin:${args.sonarQubeMavenPluginVersion}:sonar
        """
      }
    }
  }
}
