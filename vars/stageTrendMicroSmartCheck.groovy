/**
  * Scan Docker Image on Trend Micro Smart Check
  *
  * @param imgNames String or List of Docker Images name
  *                 eg. 'myapp' or ['module-1', 'module-2']
  * @param imgTag String of Docker Image Tag
  *                 eg. dev
  * @param imgRepoJenkinsCred String of Jenkins Credentials name
  * @param imgNamePrefix String of Docker Image Prefix
  *                      eg. private.registry.com/username
  * @param smartcheckHost String of Trend Micro Smart Check Hostname
  * @param smartcheckCredentialsId String of Jenkins Credentials ID type Username/Password
                                   to authentication with Trend Micro Smart Check
  * @param paramArgs Map of optional variables
  * [
  *   containerName: String                     Container name in podTemplate
  *   debug: Boolean                            Smart Check Plugin debugging flag
  *   findingsThreshold: String of JSON Object  https://github.com/deep-security/smartcheck-plugin#parameters
  * ]
  */
def call(
  imgNames,
  imgTag,
  imgRepoJenkinsCred,
  imgNamePrefix,
  smartcheckHost,
  smartcheckCredentialsId,
  Map paramArgs = [:]
) {
  // Set default optional arguments
  private def defaultArgs = [
    containerName: 'docker',
    debug: false,
    findingsThreshold: "{\\\"malware\\\":0,\\\"vulnerabilities\\\":{\\\"defcon1\\\":0,\\\"critical\\\":0,\\\"high\\\":0},\\\"contents\\\":{\\\"defcon1\\\":0,\\\"critical\\\":0,\\\"high\\\":0},\\\"checklists\\\":{\\\"defcon1\\\":0,\\\"critical\\\":0,\\\"high\\\":0}}"
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  stage('Trend Micro Smart Check Scan') {
    container(args.containerName) {

      // Convert to list if imgNames is String
      def imgNamesList = (imgNames instanceof List) ? imgNames : [imgNames]

      // In case of we scan multiple images per git repository
      imgNamesList.each { item ->

        withCredentials([
          usernamePassword([
            credentialsId: imgRepoJenkinsCred,
            usernameVariable: "REGISTRY_USER",
            passwordVariable: "REGISTRY_PASSWORD",
          ])
        ]) {
          smartcheckScan([
            debug: args.debug,
            imageName: "${imgNamePrefix}/${item}:${imgTag}",
            smartcheckHost: smartcheckHost,
            smartcheckCredentialsId: smartcheckCredentialsId,
            imagePullAuth: "{\\\"username\\\":\\\"${REGISTRY_USER}\\\",\\\"password\\\":\\\"${REGISTRY_PASSWORD}\\\"}",
            findingsThreshold: args.findingsThreshold
          ])
        }

      }

    }
  }
}
