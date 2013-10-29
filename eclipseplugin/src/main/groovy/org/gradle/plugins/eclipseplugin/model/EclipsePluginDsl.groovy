package org.gradle.plugins.eclipseplugin.model

import org.gradle.api.Project

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 27.06.13
 * Time: 16:20
 * To change this template use File | Settings | File Templates.
 */
class EclipsePluginDsl {

    boolean mirrorDependencies

    private Project project

    public EclipsePluginDsl (final Project project) {
        this.project = project
    }

}
