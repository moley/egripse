package org.gradle.plugins.eclipsebase.updatesite

import groovy.util.logging.Log
import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
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

        Eclipse eclipse = project.eclipsebase

        File updateSiteSource = project.file ("build/newUpdatesiteContent")
        File updateSiteFeaturesPath = new File (updateSiteSource, "features")
        File updateSitePluginsPath = new File (updateSiteSource, "plugins")

        //copy Features
        for (EclipseFeature feature : eclipse.workspace.getEclipseFeatures()) {
            String fromString = new File (feature.featurepath, "build/libs")
            log.info("Copy from " + fromString)
            project.copy {
                into updateSiteFeaturesPath.absolutePath
                from (fromString) {
                    include '*.jar'
                }
            }
        }

        for (EclipsePlugin plugin : eclipse.workspace.getPlugins()) {
            String fromString = new File (plugin.originPath, "build/libs")
            log.info("Copy from " + fromString)
            project.copy {
                into updateSitePluginsPath.absolutePath
                from (fromString) {
                    include '*.jar'
                }
            }
        }
    }

}
