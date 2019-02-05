package org.gradle.plugins.eclipsebase.updatesite

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.dsl.EclipseBaseDsl
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.EclipseFeature
import org.gradle.plugins.eclipsebase.model.EclipsePlugin

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 04.07.13
 * Time: 11:16
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class PrepareFeatureUpdatesiteTask extends DefaultTask {

    private String getVersion (final File jarFile) {
      return jarFile.name.substring(jarFile.name.length() - 14, jarFile.name.length() - 4) //.v - .jar
    }

    private String getLatestVersion (File featurePath) {

      if (featurePath.listFiles() == null || featurePath.listFiles().length == 0)
        throw new FileNotFoundException("No files found in path " + featurePath.absolutePath)

      String latestVersion = "";
      for (File next: featurePath.listFiles()) {
        String currentVersion = getVersion(next)
        if (currentVersion > latestVersion)
          latestVersion = currentVersion
      }

      return latestVersion


    }

    @TaskAction
    public exec () {

        Eclipse eclipse = project.eclipsemodel
        EclipseBaseDsl basedsl = project.eclipsebase

        File updateSiteSource = eclipse.localUpdatesiteContentPath
        File updateSiteFeaturesPath = new File (updateSiteSource, "features")
        File updateSitePluginsPath = new File (updateSiteSource, "plugins")

        String latestVersion = null

        //copy Features
        for (EclipseFeature feature : eclipse.workspace.eclipseFeatures) {
            File featurePath = new File (feature.featurepath, "build/libs")

            if (latestVersion == null)
              latestVersion = getLatestVersion(featurePath)

            String fromString = featurePath.absolutePath
            log.info("Copy from feature path " + fromString + " to " + updateSiteFeaturesPath.absolutePath)
            int numberOfCopiedElements = 0
            project.copy {
                into updateSiteFeaturesPath.absolutePath
                from (fromString) {
                    include "*${latestVersion}.jar"
                }
                eachFile {
                  numberOfCopiedElements ++
                }
            }
            if (numberOfCopiedElements != 1)
              throw new IllegalStateException("Not exactly 1, but " + numberOfCopiedElements + " files copied in feature path ${featurePath.absolutePath} " +
                "(detected last number $latestVersion), files: ${featurePath.listFiles()}")
        }

        for (EclipsePlugin plugin : eclipse.workspace.plugins) {
            if (! plugin.isTestPlugin()) {
              File pluginPath = new File (plugin.originPath, "build/libs")
              String fromString = new File (plugin.originPath, "build/libs").absolutePath
              log.info("Copy from plugins path " + fromString + "to " + updateSitePluginsPath.absolutePath)
              int numberOfCopiedElements = 0
              project.copy {
                into updateSitePluginsPath.absolutePath
                from (fromString) {
                    include "*${latestVersion}.jar"
                }
                eachFile {
                  numberOfCopiedElements ++
                }
              }

              if (numberOfCopiedElements != 1)
                throw new IllegalStateException("Not exactly 1, but " + numberOfCopiedElements + " files copied in feature path ${pluginPath.absolutePath}"+
                  "  (detected last number $latestVersion), files: ${pluginPath.listFiles()}")
            }
        }

        project.fileTree(updateSiteSource).each {
            File newFileName = new File (it.parentFile, it.name.replace("-","_"))
            log.info ("Rename " + it.absolutePath + " to " + newFileName.absolutePath)
            it.renameTo(newFileName)
        }


    }

}
