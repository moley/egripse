package org.gradle.plugins.eclipsebase.integrationtest

import groovy.util.logging.Slf4j
import org.apache.commons.io.FileUtils

@Slf4j
class AbstractIntegrationTest {

  /**
   * Gets path to a gradleplugins-module
   *
   * @param project name of the project, e.g. 'marvin'
   * @return path
   */
  protected File getProjectPath(final String project) {

    File pathAsPath = new File(project)
    if (!pathAsPath.exists()) {
      log.info("Path " + pathAsPath.absolutePath + " doesn't exist, look up in parent")
      pathAsPath = new File(new File("").absoluteFile.parentFile, project)
    }
    else
      log.info("Using projectpath " + pathAsPath.absolutePath)

    if (!pathAsPath.exists())
      throw new IllegalStateException("Path " + pathAsPath.absolutePath + " not found in project.")

    return pathAsPath
  }

  protected clearProject(final File projectpath) {
    FileUtils.deleteDirectory(new File(projectpath, 'build'))
  }

  protected List<String> createArguments(final String ... arguments) {
    Collection<String> allArguments = new ArrayList<String>()
    allArguments.addAll(arguments)
    allArguments.add("-s")

    File gradleProps = new File(System.getProperty("user.home"), '.gradle/gradle.properties')
    if (gradleProps.exists()) {
      Properties props = new Properties()
      props.load(new FileReader(gradleProps))
      String httpProxyHost = props.get("systemProp.http.proxyHost")
      String httpProxyPort = props.get("systemProp.http.proxyPort")
      String httpsProxyHost = props.get("systemProp.https.proxyHost")
      String httpsProxyPort = props.get("systemProp.https.proxyPort")
      if (httpProxyHost != null && ! httpProxyHost.trim().isEmpty())
        allArguments.add("-Dhttp.proxyHost=$httpProxyHost".toString())
      if (httpProxyPort != null && ! httpProxyPort.trim().isEmpty())
        allArguments.add("-Dhttp.proxyPort=$httpProxyPort".toString())
      if (httpsProxyHost != null && ! httpsProxyHost.trim().isEmpty())
        allArguments.add("-Dhttps.proxyHost=$httpsProxyHost".toString())
      if (httpsProxyPort != null && ! httpsProxyPort.trim().isEmpty())
        allArguments.add("-Dhttps.proxyPort=$httpsProxyPort".toString())

      println allArguments
    }

    return allArguments
  }
}
