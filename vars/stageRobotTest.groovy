/**
  * Run Robot Framework Test
  *
  * @param appName String of Application Name
  * @param robotFileTestsPath String of path where to run robot tests
  * @param baseUrl String of Base URL to run robot test
  * @param paramArgs Map of optional variables
  * [
  *   containerName: String Container name in podTemplate
  *   robotOutputBasePath: String Temporary Robot Output Report Base Path
  *   robotTestsPath: String Path on robot container to run tests
  *   robotReportsPath: String Path on robot container to output test report
  *   robotOutputPath: String Path to copy output test report for publish
  *   testBrowser: String browser name
  *   contextPath: String context or subpath that will append to baseUrl
  * ]
  */
def call(
  appName,
  robotFileTestsPath,
  baseUrl,
  Map paramArgs = [:]
) {

  // Set default optional arguments
  private def defaultArgs = [
    containerName: 'robot',
    robotOutputBasePath: 'test/robot/reports',
    robotTestsPath: '/opt/robotframework/tests',
    robotReportsPath: '/opt/robotframework/reports',
    testBrowser: 'chrome',
    contextPath: ''
  ]
  defaultArgs['robotOutputPath'] = "${defaultArgs.robotOutputBasePath}/${appName}"
  // Replace default optional arguments with parametered arguments
  private def args = defaultArgs << paramArgs

  try {
    stage("${appName} Acceptance Test (Robotframework)") {
      container(args.containerName) {

        // Clean tests and reports directory, copy robot test files and run test
        // then copy results to output directory
        sh """
          echo "Run ${appName} Tests"
          mkdir -p ${args.robotTestsPath} ${args.robotOutputPath}
          rm -rf ${args.robotTestsPath}/* ${args.robotReportsPath}/*
          cp -av ${robotFileTestsPath} ${args.robotTestsPath}/
          export BROWSER=${args.testBrowser}
          export BASE_URL=${baseUrl}
          export CONTEXT_PATH=${contextPath}
          run-tests-in-virtual-screen.sh

          rm -rf ${args.robotOutputPath}/*
          cp -av ${args.robotReportsPath}/* ${args.robotOutputPath}/
        """
      }
    }
  } catch(Exception e) {
    currentBuild.result = 'FAILURE'
  }
}
