/**
  * Prepare podTemplate
  *
  * @param podType String to map podTemplate with inPodMap
  * @param projectName String of project name
  * @param k8sCloudName String of Jenkins Kubernetes Cloud Name
  * @param podTemplateArgs Map is the same as podTemplate
  * @param body Closure to run steps within node()
  */
def call(
  String podType,
  String projectName,
  String k8sCloudName = projectName,
  Map podTemplateArgs = [:],
  Closure body
) {

  // Assign default containers and volumes for each type of deployment
  // Please bump version if you update containers or volumes
  private static final inPodMap = [
    java: [
      version: "0.2.0",
      containers: [
        // Don't use alpine version. It having problem with forking JVM such as running surefire and junit testing
        // https://hub.docker.com/_/openjdk?tab=tags
        containerTemplate(name: 'java', image: 'openjdk:11.0.5-jdk-stretch', ttyEnabled: true, command: 'cat'),
        // https://hub.docker.com/_/maven?tab=tags
        containerTemplate(name: 'maven', image: 'maven:3.6.3-jdk-11', ttyEnabled: true, command: 'cat'),
        // https://hub.docker.com/_/docker?tab=tags
        containerTemplate(name: 'docker', image: 'docker:19.03.5-dind', ttyEnabled: true, privileged: true,
          command: 'dockerd --host=unix:///var/run/docker.sock --host=tcp://0.0.0.0:2375 --storage-driver=overlay2'),
        // https://hub.docker.com/r/lachlanevenson/k8s-helm/tags
        containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:v3.0.2', ttyEnabled: true, command: 'cat'),
        // https://hub.docker.com/r/lachlanevenson/k8s-kubectl/tags
        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.16.4', ttyEnabled: true, command: 'cat'),
        // https://hub.docker.com/r/ppodgorsek/robot-framework/tags
        containerTemplate(name: 'robot', image: 'ppodgorsek/robot-framework:3.5.0', ttyEnabled: true, command: 'cat')
      ],
      volumes: [
        // Don't use NFS, It does not works well
        emptyDirVolume(mountPath: '/var/lib/docker')
      ]
    ]
  ]

  private def defaultArgs = [
    label: projectName + '-' + inPodMap[podType]['version'],
    cloud: k8sCloudName,
    idleMinutes: 360,
    containers: inPodMap[podType]['containers'],
    volumes: inPodMap[podType]['volumes']
  ]

  // For containers, add the lists together, but remove duplicates by name,
  // giving precedence to the user specified args.
  private def finalContainers = addWithoutDuplicates((podTemplateArgs.containers ?: []), defaultArgs.containers) { it.getArguments().name }
  private def finalArgs = defaultArgs << podTemplateArgs << [containers: finalContainers]

  podTemplate(finalArgs) {
    node(finalArgs.label) {
      body()
    }
  }

}

// Grab from here
// https://github.com/salemove/pipeline-lib/blob/master/src/com/salemove/Collections.groovy
private def addWithoutDuplicates(precedenceList, otherList, Closure selector) {
  def otherListFiltered = otherList.findAll { otherItem ->
    def isInPrecedenceList = precedenceList.any { precedenceItem ->
      selector(precedenceItem) == selector(otherItem)
    }
    !isInPrecedenceList
  }
  precedenceList + otherListFiltered
}
