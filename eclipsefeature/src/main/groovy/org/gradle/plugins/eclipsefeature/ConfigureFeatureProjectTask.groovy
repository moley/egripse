package org.gradle.plugins.eclipsefeature

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.config.BuildPropertiesConfigurator
import org.gradle.plugins.eclipsebase.config.ProjectVersionConfigurator
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.EclipseFeature
import org.gradle.plugins.eclipsebase.model.FeatureXml

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 11.10.13
 * Time: 12:58
 * To change this template use File | Settings | File Templates.
 */
class ConfigureFeatureProjectTask extends DefaultTask{

    private ProjectVersionConfigurator projectVersionConfigurator = new ProjectVersionConfigurator()

    private BuildPropertiesConfigurator buildPropertiesConfigurator = new BuildPropertiesConfigurator()

    @TaskAction
    public void configure () {

        Eclipse eclipseModel = project.rootProject.eclipsemodel
        EclipseFeature eclipseFeature = eclipseModel.workspace.findFeatureByPath(project.projectDir)
        FeatureXml featureXml = eclipseFeature.featureXml

        buildPropertiesConfigurator.synchronizeResourcesFromBuildProperties(project, eclipseFeature.buildProperties)
        projectVersionConfigurator.setVersion(project, featureXml.version, featureXml.featureID)

    }
}
