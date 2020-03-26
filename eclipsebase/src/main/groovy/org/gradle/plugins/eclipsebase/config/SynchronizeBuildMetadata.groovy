package org.gradle.plugins.eclipsebase.config

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.model.BuildProperties
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.EclipseFeature
import org.gradle.plugins.eclipsebase.model.EclipsePlugin

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 12.11.13
 * Time: 15:44
 * To change this template use File | Settings | File Templates.
 */
class SynchronizeBuildMetadata extends DefaultTask{

    public final static String TASKNAME_SYNC_BUILD_METADATA = "syncBuildMetadata"


    private BuildPropertiesConfigurator buildPropertiesConfigurator = new BuildPropertiesConfigurator()

    @TaskAction
    public void synchronize () {
        project.logger.info ("Synchronize build metadata in project " + project.name)

        Eclipse eclipseModel = project.rootProject.eclipsemodel
        EclipseFeature eclipseFeature = eclipseModel.workspace.findFeatureByPath(project.projectDir)
        EclipsePlugin eclipsePlugin = eclipseModel.workspace.findPluginByPath(project.projectDir)

        BuildProperties buildProps = eclipseFeature != null ? eclipseFeature.buildProperties : eclipsePlugin.buildProperties

        buildPropertiesConfigurator.synchronizeResourcesFromBuildProperties(project, buildProps, eclipsePlugin != null, eclipseFeature != null)

    }
}
