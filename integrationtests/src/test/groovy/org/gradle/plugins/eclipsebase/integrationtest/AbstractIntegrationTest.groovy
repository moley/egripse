package org.gradle.plugins.eclipsebase.integrationtest

import groovy.util.logging.Slf4j

@Slf4j
class AbstractIntegrationTest {

  /**
   * Gets path to a gradleplugins-module
   *
   * @param project name of the project, e.g. 'marvin'
   * @return path
   */
  public File getProjectPath(final String project) {

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
}
