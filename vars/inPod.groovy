// Prepare podTemplate
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
    maven: [
      version: "0.1.0",
      containers: [
        // Don't use alpine version. It having problem with forking JVM such as running surefire and junit testing
        containerTemplate(name: 'java', image: 'openjdk:11.0.5-jdk-stretch', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'maven', image: 'maven:3.6.3-jdk-11-slim', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'docker', image: 'docker:19.03.5-dind', ttyEnabled: true, privileged: true,
          command: 'dockerd --host=unix:///var/run/docker.sock --host=tcp://0.0.0.0:2375 --storage-driver=overlay2'),
        containerTemplate(name: 'helm', image: 'lachlanevenson/k8s-helm:v2.16.1', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl:v1.16.3', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'robot', image: 'ppodgorsek/robot-framework:3.4.0', ttyEnabled: true, command: 'cat')
      ],
      volumes: [
        // Mount NFS as PVC for caching
        persistentVolumeClaim(claimName: "nfs-${projectName}-jenkins-slave-dependency-check",
          mountPath: '/home/jenkins/dependency-check-data'),
        persistentVolumeClaim(claimName: "nfs-${projectName}-jenkins-slave-m2",
          mountPath: '/root/.m2'),
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
