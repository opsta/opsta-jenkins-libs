/**
  * Run SonarQube Analysis with Maven
  *
  * @param sonarQubeEnv String of SonarQube Environment Name in Jenkins
  * @param paramArgs Map of optional variables
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
  Map paramArgs = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    sonarQubeMavenPluginVersion: "3.7.0.1746",
    mavenSettingsFilePath: '',
    containerName: 'maven'
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  stage('SonarQube Analysis with Maven') {
    container(args.containerName) {
      withSonarQubeEnv(sonarQubeEnv) {

        // Check if need custom maven settings
        mavenSettingsFilePathParameter = ''
        if(!args.mavenSettingsFilePath.isEmpty()) {
          mavenSettingsFilePathParameter = "-s ${args.mavenSettingsFilePath}"
        }

        sh """
          mvn ${mavenSettingsFilePathParameter} -e \
            org.sonarsource.scanner.maven:sonar-maven-plugin:${args.sonarQubeMavenPluginVersion}:sonar
        """
      }
    }
  }
}
