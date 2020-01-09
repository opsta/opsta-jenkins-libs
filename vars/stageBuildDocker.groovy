/**
  * Build and push Docker Image
  *
  * @param imgNames List of Docker Images name
  *                 eg. ['module-1', 'module-2']
  * @param imgTag String of Docker Image Tag
  *                 eg. dev
  * @param imgRepoServerUrl String of Private Docker Registry Server Url 
  *                         that you want to authen
  *                         eg. https://registry.hub.docker.com
  * @param imgRepoJenkinsCred String of Jenkins Credentials name
  * @param imgNamePrefix String of Docker Image Prefix
  *                      eg. private.registry.com/username
  * @param paramArgs Map of optional variables
  * [
  *   containerName: String           Container name in podTemplate
  *   mavenSettingsFilePath: String   Custom Maven settings file path
  *                                   eg. ./.m2/maven-mirror-settings.yml
  * ]
  */
def call(
  imgNames,
  imgTag,
  imgRepoServerUrl,
  imgRepoJenkinsCred,
  imgNamePrefix,
  Map paramArgs = [:]
) {
  // Set default optional arguments
  private def defaultArgs = [
    containerName: 'docker',
    mavenSettingsFilePath: ''
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  stage('Build and Push Docker Image') {
    container(args.containerName) {

      // Enable Docker Buildkit to improve build speed and enable new features
      // Require Docker > 18.09
      withEnv(['DOCKER_BUILDKIT=1']) {

        // Authen with Private Registry
        docker.withRegistry("${imgRepoServerUrl}", "${imgRepoJenkinsCred}") {

          // In case of we build multiple images per git repository
          imgNames.each { item ->

            // Build variables
            imgFullName = "${imgNamePrefix}/${item}"
            if (imgNames.size() > 1) {
              dockerfile = "Dockerfile.${item}"
            } else {
              dockerfile = "Dockerfile"
            }
            echo "Start building ${item} image [${imgFullName}:${imgTag}]"

            // Search and replace Maven custom settings file parameter
            if(!args.mavenSettingsFilePath.isEmpty()) {
              sh "sed -i 's!\\(RUN .*\\)-s .* \\(.*\\)!\\1-s ${args.mavenSettingsFilePath} \\2!g' ${dockerfile}"
            }

            // Build and Push Docker Image
            docker.build("${imgFullName}:${imgTag}", "-f ${dockerfile} --pull .").push()
          }
        }
      }
    }
  }
}
