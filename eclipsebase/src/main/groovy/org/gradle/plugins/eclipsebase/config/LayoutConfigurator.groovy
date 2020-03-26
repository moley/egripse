package org.gradle.plugins.eclipsebase.config


import org.gradle.api.Project

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 14.11.13
 * Time: 09:47
 * To change this template use File | Settings | File Templates.
 */
class LayoutConfigurator {

    /**
     * configurations, which is relevant for features and plugins
     * @param project  project
     */
    public void configure (final Project project) {
        project.logger.info("Configure projectlayout for project ${project.name} as sourceproject")
        project.sourceSets.main.resources.srcDirs 'build/mergedResources'
    }
}
