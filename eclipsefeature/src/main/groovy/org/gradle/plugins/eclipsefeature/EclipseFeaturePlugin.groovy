package org.gradle.plugins.eclipsefeature

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.plugins.eclipsebase.config.LayoutConfigurator
import org.gradle.plugins.eclipsebase.config.SynchronizeBuildMetadata
import org.gradle.plugins.ide.eclipse.model.EclipseModel

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 03.07.13
 * Time: 08:06
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class EclipseFeaturePlugin implements Plugin<Project>  {

    private LayoutConfigurator layoutconfigurator = new LayoutConfigurator()


    @Override
    void apply(Project project) {

        log.info ("Applying plugin ${getClass()} in project ${project.name}")

        SynchronizeBuildMetadata syncBuildproperties = project.tasks.create(type:SynchronizeBuildMetadata, name:SynchronizeBuildMetadata.TASKNAME_SYNC_BUILD_METADATA)
        project.tasks.processResources.dependsOn syncBuildproperties

        DefaultTask buildTask = project.tasks.findByName("build")

        ConfigureFeatureProjectTask configureBuildTask = project.tasks.create(type:ConfigureFeatureProjectTask, name:"configureBuild")
        buildTask.dependsOn configureBuildTask

        configureProjectFiles(project)

        layoutconfigurator.configure(project)

    }

    void configureProjectFiles (final Project project) {
        EclipseModel eclipsemodel = project.extensions.findByType(EclipseModel)
        eclipsemodel.project {
            natures 'org.eclipse.pde.FeatureNature'
            buildCommand 'org.eclipse.pde.FeatureBuilder'
        }
    }
}
