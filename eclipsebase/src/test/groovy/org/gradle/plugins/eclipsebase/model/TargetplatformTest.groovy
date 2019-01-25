package org.gradle.plugins.eclipsebase.model

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test


class TargetplatformTest {

  @Test
  public void read () {

    File expectedFile = new File ("src/test/resources/targetplatformOomph/build/.goomph/shared-bundles/plugins/com.google.gson_2.8.2.v20180104-1110.jar").absoluteFile
    Project project = ProjectBuilder.builder().withProjectDir(new File ("src/test/resources/targetplatformOomph")).build()
    Targetplatform targetplatformOomph = new Targetplatform(project)
    List <EclipsePlugin> plugins = targetplatformOomph.read()
    Assert.assertTrue ("Expected file does not exist", plugins.get(0).originPath.exists())
    Assert.assertEquals ("Expected file does not exist", expectedFile, plugins.get(0).originPath)

  }
}
