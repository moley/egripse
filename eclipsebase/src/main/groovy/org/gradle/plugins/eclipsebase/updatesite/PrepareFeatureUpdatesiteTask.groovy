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

    @TaskAction
    public exec () {

        Eclipse eclipse = project.eclipsemodel
        EclipseBaseDsl basedsl = project.eclipsebase

        File updateSiteSource = project.file ("build/newUpdatesiteContent")
        File updateSiteFeaturesPath = new File (updateSiteSource, "features")
        File updateSitePluginsPath = new File (updateSiteSource, "plugins")

        /**
         * project.configurations.archives.artifacts.each {
         File artifactFile = it.file
         String nameWithUnderscore = artifactFile.name.replace("-", "_")
         it.file = new File (artifactFile.parentFile, nameWithUnderscore)
         log.info ("Artifact file in project " + project.name + " set to " + it.file.absolutePath)
         }
         */

        //copy Features
        for (EclipseFeature feature : eclipse.workspace.eclipseFeatures) {
            File featurePath = new File (feature.featurepath, "build/libs")
            if (featurePath.exists() && featurePath.listFiles() != null && featurePath.listFiles().length > 1)
                throw new IllegalStateException("Only one feature jar is allowed to be added to updatesiteContent. Please do a clean build for feature " + featurePath.absolutePath)
            String fromString = featurePath.absolutePath
            log.info("Copy from feature path " + fromString + " to " + updateSiteFeaturesPath.absolutePath)
            project.copy {
                into updateSiteFeaturesPath.absolutePath
                from (fromString) {
                    include '*.jar'
                }
            }
        }

        for (EclipsePlugin plugin : eclipse.workspace.plugins) {
            if (! plugin.isTestPlugin()) {
              File pluginPath = new File (plugin.originPath, "build/libs")
              if (pluginPath.exists() && pluginPath.listFiles() != null && pluginPath.listFiles().length > 1)
                    throw new IllegalStateException("Only one plugin jar is allowed to be added to updatesiteContent. Please do a clean build for project " + pluginPath.absolutePath)
              String fromString = new File (plugin.originPath, "build/libs").absolutePath
              log.info("Copy from plugins path " + fromString + "to " + updateSitePluginsPath.absolutePath)
              project.copy {
                into updateSitePluginsPath.absolutePath
                from (fromString) {
                    include '*.jar'
                }
              }
            }
        }

        project.fileTree(updateSiteSource).each {
            File newFileName = new File (it.parentFile, it.name.replace("-","_"))
            log.info ("Rename " + it.absolutePath + " to " + newFileName.absolutePath)
            it.renameTo(newFileName)
        }


    }

}
