package org.gradle.plugins.eclipsebase.updatesite

import com.google.common.io.Files
import org.gradle.api.Project
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test

class PrepareFeatureUpdatesiteTaskTest {

  private static void createFile (final File file) {
    file.parentFile.mkdirs()
    file.createNewFile()
  }
  @Test
  public void getLatest () {
    String firstNumber = '1.0.0.v201902040808'
    String secondNumber = '1.0.0.v201902040818'

    File featuresDir = Files.createTempDir()
    createFile(new File (featuresDir, "build/libs/some.ugly.feature-${firstNumber}.jar"))
    createFile(new File (featuresDir, "build/libs/some.ugly.feature-${secondNumber}.jar"))

    File plugin1Dir = Files.createTempDir()
    createFile(new File (plugin1Dir, "build/libs/some.ugly.plugin1-${firstNumber}.jar"))
    createFile(new File (plugin1Dir, "build/libs/some.ugly.plugin1-${secondNumber}.jar"))

    File plugin2Dir = Files.createTempDir()
    createFile(new File (plugin2Dir, "build/libs/some.ugly.plugin2-${firstNumber}.jar"))
    createFile(new File (plugin2Dir, "build/libs/some.ugly.plugin2-${secondNumber}.jar"))

    Project project = ProjectBuilder.builder().build()
    project.plugins.apply('eclipsebase')


    Eclipse eclipse = project.eclipsemodel
    eclipse.workspace.addEclipseFeature(featuresDir)
    eclipse.workspace.addEclipsePlugin(plugin1Dir)
    eclipse.workspace.addEclipsePlugin(plugin2Dir)
    PrepareFeatureUpdatesiteTask prepareFeatureUpdatesiteTask = project.task("prepare", type: PrepareFeatureUpdatesiteTask)
    prepareFeatureUpdatesiteTask.exec()

    File updateSiteSource = eclipse.localUpdatesiteContentPath
    File updateSiteFeaturesPath = new File (updateSiteSource, "features")
    Assert.assertEquals ("Number of prepared features invalid", 1, updateSiteFeaturesPath.listFiles().length)
    File updateSitePluginsPath = new File (updateSiteSource, "plugins")
    Assert.assertEquals ("Number of prepared plugins invalid", 2, updateSitePluginsPath.listFiles().length)

  }

  @Test(expected = IllegalStateException.class)
  public void getLatestFailsOnDifferentVersions () {
    String firstNumber = '1.0.0.v201902040808'
    String secondNumber = '1.0.0.v201902040818'

    File featuresDir = Files.createTempDir()
    createFile(new File (featuresDir, "build/libs/some.ugly.feature-${firstNumber}.jar"))
    createFile(new File (featuresDir, "build/libs/some.ugly.feature-${secondNumber}.jar"))

    File plugin1Dir = Files.createTempDir()
    createFile(new File (plugin1Dir, "build/libs/some.ugly.plugin1-${firstNumber}.jar"))

    File plugin2Dir = Files.createTempDir()
    createFile(new File (plugin2Dir, "build/libs/some.ugly.plugin2-${firstNumber}.jar"))
    createFile(new File (plugin2Dir, "build/libs/some.ugly.plugin2-${secondNumber}.jar"))

    Project project = ProjectBuilder.builder().build()
    project.plugins.apply('eclipsebase')


    Eclipse eclipse = project.eclipsemodel
    eclipse.workspace.addEclipseFeature(featuresDir)
    eclipse.workspace.addEclipsePlugin(plugin1Dir)
    eclipse.workspace.addEclipsePlugin(plugin2Dir)
    PrepareFeatureUpdatesiteTask prepareFeatureUpdatesiteTask = project.task("prepare", type: PrepareFeatureUpdatesiteTask)
    prepareFeatureUpdatesiteTask.exec()

  }
}
