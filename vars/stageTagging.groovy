/**
  * Tagging Docker Image and Git
  *
  * @param imgName String of Docker Image Name
                   eg. service-a
  * @param imgFullName String of Docker Image Name with prefix
                       eg. registry.example.com/myapp/service-a
  * @param imgUatTag String of Docker UAT Image Tag
                     eg. uat or staging
  * @param imgPrdTag String of Docker Production Image Tag
                     eg. build-13 or v1.0.2
  * @param imgRepoServerUrl String of Docker private registry URL to use with docker login
  * @param imgRepoJenkinsCred String of Jenkins Credential to authen with Docker private registry
  * @param gitPushJenkinsCred String of Jenkins Credential to pull and push source code
  * @param gitCloneUrl String of URL to use with git clone
  * @param paramArgs Map of optional variables
  * [
  *   dockerContainerName: String Docker Container name in podTemplate
  *   gitContainerName: String Git Container name in podTemplate
  * ]
  */
def call(
  imgNames,
  imgFullName,
  imgUatTag,
  imgPrdTag,
  imgRepoServerUrl,
  imgRepoJenkinsCred,
  gitPushJenkinsCred,
  gitCloneUrl,
  Map paramArgs = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    dockerContainerName: 'docker',
    gitContainerName: 'git'
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  stage("Docker tag ${imgNames} production image") {
    container(args.dockerContainerName) {
      docker.withRegistry(imgRepoServerUrl, imgRepoJenkinsCred) {
        uatImage = docker.image("${imgFullName}:${imgUatTag}")
        uatImage.pull()
        uatImage.push(imgPrdTag)
      }
    }
  }

  stage('Git tag version') {
    container(gitContainerName) {
      sshagent(credentials: [gitPushJenkinsCred]) {
        checkout([
          $class: 'GitSCM',
          branches: [[name: 'refs/heads/master']],
          userRemoteConfigs: [[
            credentialsId: gitPushJenkinsCred,
            url: gitCloneUrl
          ]]
        ])
        sh """
          git tag ${imgPrdTag}
          SSH_AUTH_SOCK=${env.SSH_AUTH_SOCK} \
            GIT_SSH_COMMAND="ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no" \
            git push --tags
        """
      }
    }
  }
}
