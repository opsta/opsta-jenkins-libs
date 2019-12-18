/**
  * Publish Robot Framework Result
  *
  * @param paramArgs Map of optional variables
  * [
  *   containerName: String Container name in podTemplate
  *   robotOutputBasePath: String Temporary Robot Output Report Base Path
  *   passThreshold: Integer Percent of test to pass threshold
  *   unstableThreshold: Integer Percent of test threshold to make build unstable
  * ]
  */
def call(
  Map paramArgs = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    containerName: 'robot',
    robotOutputBasePath: 'test/robot/reports',
    passThreshold: 100,
    unstableThreshold: 0
  ]
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  stage("Publish Robot Framework Result") {
    container(args.containerName) {
      step([
        $class: 'RobotPublisher',
        disableArchiveOutput: true,
        outputPath: "${robotOutputBasePath}",
        logFileName: '**/log.html',
        outputFileName: '**/output.xml',
        reportFileName: '**/report.html',
        otherFiles: '**/*screenshot*.png',
        passThreshold: args.passThreshold,
        unstableThreshold: args.unstableThreshold
      ])
    }
  }
}
