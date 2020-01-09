/**
  * Run OWASP Dependency Check with Maven
  *
  * @param paramArgs Map of optional variables
  * [
  *   containerName: String       Container name in podTemplate
  *   odcInstallation: String     OWASP Dependency Check Installation Name in Jenkins
  *   owaspReportFileName: String OWASP output report file name
  *   owaspDataPath: String       OWASP Download Data Path
  * ]
  */
def call(
  Map paramArgs = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    odcInstallation: 'dependency-check',
    containerName: 'maven',
    owaspReportFileName: 'dependency-check-report.xml',
    owaspDataPath: '/home/jenkins/dependency-check-data'
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  stage('Run OWASP Dependency Check with Maven') {
    container(args.containerName) {
      dependencycheck(
        additionalArguments: "--out ${args.owaspReportFileName} --data ${args.owaspDataPath}", 
        odcInstallation: args.odcInstallation
      )
      dependencyCheckPublisher(
        pattern: args.owaspReportFileName
      )
    }
  }
}
