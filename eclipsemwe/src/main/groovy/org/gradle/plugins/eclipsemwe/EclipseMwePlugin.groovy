package org.gradle.plugins.eclipsemwe

import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.DefaultSourceDirectorySet
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
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

        EclipseMwe mweDsl = project.extensions.create("eclipsemwe", EclipseMwe, project)
        EclipseMwe mwe2Dsl = project.extensions.create("eclipsemwe2", EclipseMwe, project)

        log.info("Create mwe task")

        SourceSet sourceSetMain = project.sourceSets.main

        project.afterEvaluate { //because layout is done in afterEvaluate

            SourceDirectorySet directorySetMwe = new DefaultSourceDirectorySet("mwe workflow", project.fileResolver)
            directorySetMwe.filter.includes = ['**/*.mwe', '**/*.xtext', '**/*.ecore']
            sourceSetMain.java.srcDirs.each {
                if (! it.name.contains('gen'))
                  directorySetMwe.srcDir(it)
            }

            if (! mweDsl.mweFiles.isEmpty()) {
                EclipseMweTask mweTask = project.tasks.create("generateMwe", EclipseMweTask)
                mweTask.description = "Starting mwe generator"
                mweTask.basedir = project.projectDir
                mweTask.source = directorySetMwe
                mweTask.outputDir = project.file("src-gen")
                log.info("Mwe Generator is configured to be called from " + project.projectDir.absolutePath)

                String compileJavaTaskName = project.sourceSets.main.compileJavaTaskName
                project.tasks.getByName(compileJavaTaskName).dependsOn(mweTask)
                log.info("mwetask " + mweTask.name + " dependend in " + compileJavaTaskName)
            }

            SourceDirectorySet directorySetMwe2 = new DefaultSourceDirectorySet("mwe2 workflow", project.fileResolver)
            directorySetMwe2.filter.includes = ['**/*.mwe2', '**/*.xtext', '**/*.ecore']
            sourceSetMain.java.srcDirs.each {
                if (! it.name.contains('gen'))
                    directorySetMwe2.srcDir(it)
            }

            if (! mwe2Dsl.mweFiles.isEmpty()) {
                EclipseMwe2Task mwe2Task = project.tasks.create("generateMwe2", EclipseMwe2Task)
                mwe2Task.description = "Starting mwe2 generator"
                mwe2Task.basedir = project.projectDir
                mwe2Task.source = directorySetMwe2
                mwe2Task.outputDir = project.file("src-gen")
                log.info("Mwe2 Generator is configured to be called from " + project.projectDir.absolutePath)

                String compileJavaTaskName = project.sourceSets.main.compileJavaTaskName
                project.tasks.getByName(compileJavaTaskName).dependsOn(mwe2Task)
                log.info("mwetask " + mwe2Task.name + " dependend in " + compileJavaTaskName)
            }
        }






        //Add additional files as resources
        Copy task = project.tasks.findByName("processResources")
        Copy copyMweAdditions = project.tasks.create(type:Copy, name:'copyMweFiles')
        copyMweAdditions.destinationDir = project.file ('build/resources/main')
        copyMweAdditions.from (['src-gen', 'src'])
        copyMweAdditions.include('**/*.xmi')
        copyMweAdditions.include('**/*.xtextbin')
        copyMweAdditions.include('**/*.tokens')
        copyMweAdditions.include("**/*.g")
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
