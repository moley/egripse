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
    String testprojectFor = null
    boolean sourceproject = false

    private Project project

    public EclipsePluginDsl (final Project project) {
        this.project = project
    }

    /**
     * if the project is a default testproject
     * @param project the project for which I am a testproject
     *
     * (only contains test sources in src)
     */
    public void testproject (String forProject) {
        if (forProject == null)
            throw new NullPointerException("Param forProject must not be null");
        this.testprojectFor = forProject
        checkType()
    }

    /**
     * if the project is a default sourceproject
     * (only contains sources in src)
     */
    public void sourceproject () {
        this.sourceproject = true
        checkType()
    }

    private void checkType () {
        if (sourceproject && testprojectFor != null)
            throw new IllegalStateException("An eclipse plugin can be either an sourceproject with src path or a testproject related to another sourceplugin with src path or a maven " +
                                            "layouted project, which can contain both (default). If you want to override this you have to define your sourceSets on your own")
    }

}
