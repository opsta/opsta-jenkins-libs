// Run OWASP Dependency Check with Maven
def call(
	String odcInstallation = 'dependency-check',
  String containerName = 'maven',
	String owaspReportFileName = 'dependency-check-report.xml',
	String owaspDataPath = '/home/jenkins/dependency-check-data'
) {
  stage('Run OWASP Dependency Check with Maven') {
    container(containerName) {
      dependencycheck(
        additionalArguments: "--out ${owaspReportFileName} --data ${owaspDataPath}", 
        odcInstallation: odcInstallation
      )
      dependencyCheckPublisher(
        pattern: owaspReportFileName
      )
    }
  }
}
