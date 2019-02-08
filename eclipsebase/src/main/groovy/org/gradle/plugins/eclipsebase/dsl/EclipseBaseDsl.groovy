package org.gradle.plugins.eclipsebase.dsl

import org.gradle.api.Project

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 11.10.13
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 */
class EclipseBaseDsl {

    Collection<String> integrationtests = new ArrayList<String>()

    String pluginsPath

    String featuresPath

    UpdatesiteDsl updatesite

    Project project


    public EclipseBaseDsl (final Project project) {
        this.project = project
        integrationtests.add('**/**/integrationtest/**')
        pluginsPath = project.projectDir.absolutePath
        featuresPath = project.projectDir.absolutePath
    }

    public void integrationtest (final String integrationtest) {
        integrationtests.add(integrationtest)
    }





}
