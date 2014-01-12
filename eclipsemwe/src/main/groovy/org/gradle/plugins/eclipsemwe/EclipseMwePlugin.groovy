package org.gradle.plugins.eclipsemwe

import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.plugins.eclipseplugin.EclipsePluginPlugin

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 23:49
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class EclipseMwePlugin implements Plugin<Project>  {

    @Override
    void apply(Project project) {
        log.info("Applying plugin ${getClass()} in project ${project.name}")

        project.plugins.apply(EclipsePluginPlugin)

        project.extensions.create("eclipsemwe", EclipseMwe, project)

        log.info("Create mwe task")
        EclipseMweTask mweTask = project.tasks.create("generateMwe", EclipseMweTask)
        mweTask.description = "Starting mwe generator"
        mweTask.basedir = project.projectDir
        log.info("Mwe Generator is configured to be called from " + project.projectDir.absolutePath)

        String compileJavaTaskName = project.sourceSets.main.compileJavaTaskName
        project.tasks.getByName(compileJavaTaskName).dependsOn(mweTask)
        log.info ("mwetask " + mweTask.name + " dependend in " + compileJavaTaskName)

        //Add msi files TODO make nicer
        Copy task = project.tasks.findByName("processResources")
        Copy copyMweAdditions = project.tasks.create(type:Copy, name:'copyMweFiles')
        copyMweAdditions.destinationDir = project.file ('build/resources/main')
        copyMweAdditions.from (['src-gen', 'src'])
        copyMweAdditions.include('**/*.xmi')
        copyMweAdditions.include("**/*.g")
        copyMweAdditions.include("**/*.tokens")
        copyMweAdditions.include("**/*.xtext")
        copyMweAdditions.include("**/*.ecore")
        copyMweAdditions.include("**/*.ext")
        copyMweAdditions.include("**/*.xsd")
        copyMweAdditions.includeEmptyDirs = false
        task.dependsOn(copyMweAdditions)

        project.eclipseplugin {
            additionalCleanablePath('src-gen')
        }




    }

}
