package org.gradle.plugins.eclipseplugin.model

import org.gradle.api.Project
import org.gradle.plugins.eclipsebase.dsl.MavenizeItem

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

    Set <String> additionalCleanablePath = new HashSet<String>()

    Set<String> additionalSourceDir = new HashSet<String> ()

    Collection<MavenizeItem> mavenizeItems = new ArrayList<>()


    public EclipsePluginDsl (final Project project) {
        this.project = project
        additionalCleanablePath.add("build")
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
     * adds a path to be removed when calling clean
     * @param cleanablePath
     */
    public void additionalCleanablePath (String cleanablePath) {
        additionalCleanablePath.add(cleanablePath)
    }

    /**
     * adds a path to be added as sourcepath (e.g. src-gen)
     * @param sourcepath added sourcepath
     */
    public void additionalSourceDir (String sourcepath) {
        additionalSourceDir.add(sourcepath)
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

    /**
     * creates tasks per mavenize item to upload a mavenized artifact to
     * @param name              name of the scenario, is used to create taskname
     * @param group             group of artifact
     * @param origin            origin name of artifact
     * @param excludes          excludes to be applied to jarfile before providing
     */
    public void mavenize (final String name,
                          final String group,
                          final String origin,
                          final String...excludes) {
        mavenizeItems.add(new MavenizeItem(name: name, group: group, origin: origin, excludes: excludes))
    }

}
