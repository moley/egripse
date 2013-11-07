package org.gradle.plugins.eclipseplugin

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.internal.tasks.DefaultSourceSet
import org.gradle.api.java.archives.Manifest
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.eclipsebase.dsl.EclipseBaseDsl
import org.gradle.plugins.eclipseplugin.model.EclipsePluginDsl
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 11.10.13
 * Time: 12:37
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class ConfigurePluginProjectTask extends DefaultTask{



    @TaskAction
    public void configure () {

        EclipseBaseDsl eclipseBaseDsl = project.rootProject.eclipsebase
        EclipsePluginDsl eclipsePluginDsl = project.eclipseplugin

        configureManifest(project) //Called after version is set

        configureTests(project, eclipseBaseDsl)
        configureJacoco(project, eclipsePluginDsl )


    }

    private void configureTests (final Project project, final EclipseBaseDsl eclipse) {
        //Mark tests as integrationtests
        Test testTask = project.tasks.findByName("test")
        log.debug ("Mark tests as integrationtests: " + eclipse.integrationtests)
        testTask.exclude(eclipse.integrationtests)
        //testTask.classpath += EclipseBuildUtils.createClasspath(project)

        testTask.jvmArgs += "-Xmx1024m"
        testTask.jvmArgs += "-XX:MaxPermSize=360m"

        testTask.classpath += project.files('src')
        testTask.classpath += project.files('src-gen')

        testTask.workingDir = project.projectDir //TODO make configurable

    }

    private void configureManifest (final Project project) {
        //Add infos from MANIFEST.MF to jarfile
        Manifest manifest = project.manifest
        manifest.from (project.file("META-INF/MANIFEST.MF"))
        log.info("Attributes = " + manifest.attributes.keySet())
        manifest.attributes.put("Bundle-Version", project.version.toString())
        log.info("Version: " + manifest.attributes.get("Bundle-Version").toString())

        File manifestFile = project.file("build/resources/main/META-INF/MANIFEST.MF")
        if (manifestFile.exists())
          project.jar.manifest.from (manifestFile)
    }



    public void configureJacoco (Project project, EclipsePluginDsl pluginDsl) {

        if ((pluginDsl.testprojectFor) != null) {
            log.info("Configure jacoco to project " + project.name)
            Project sourceproject = project.rootProject.project(pluginDsl.testprojectFor)
            log.info("Found sourceproject " + sourceproject.name + " for testproject " + project.name)
            DefaultSourceSet mainSourceset = sourceproject.sourceSets.main

            project.tasks.withType(JacocoReport.class).each {
               log.info("Configure jacoco task " + it.name + "with data from project " + sourceproject.name)
               it.sourceDirectories = mainSourceset.java
               it.classDirectories = mainSourceset.output
            }

        }
    }
}
