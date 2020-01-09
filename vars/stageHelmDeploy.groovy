/**
  * Run OWASP Dependency Check with Maven
  *
  * @param projectName String of Project Name
  * @param appName String of Application Name
  * @param envName String of Environment Name
  *                eg. dev
  * @param kubeConfigJenkinsCred String of kubeconfig Jenkins Credentials name
  * @param scmVars Object from checkout scm in Jenkinsfile
  * @param paramArgs Map of optional variables
  * [
  *   containerName: String Container name in podTemplate
  *   helmValueFile: String Helm value file path
  *   helmChartPath: String Helm Chart directory path
  * ]
  */
def call(
  projectName,
  appName,
  envName,
  kubeConfigJenkinsCred,
  scmVars,
  Map paramArgs = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    containerName: 'helm',
    helmValueFile: "k8s/helm-values/${envName}/values-${projectName}-${envName}-${appName}.yaml",
    helmChartPath: 'k8s/helm'
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  stage("Deploy ${appName}") {
    container(args.containerName) {
      withCredentials([file(credentialsId: kubeConfigJenkinsCred, variable: 'KUBECONFIG')]) {
        sh """
          mkdir -p ~/.kube/
          cat $KUBECONFIG > ~/.kube/config
          sed -i 's/COMMIT_ID: CHANGE_COMMIT_ID/COMMIT_ID: ${scmVars.GIT_COMMIT}/g' ${args.helmValueFile}
          helm upgrade -i -f ${args.helmValueFile} --namespace ${projectName}-${envName} --wait \
            ${projectName}-${envName}-${appName} ${args.helmChartPath}
          """
      }
    }
  }
}
