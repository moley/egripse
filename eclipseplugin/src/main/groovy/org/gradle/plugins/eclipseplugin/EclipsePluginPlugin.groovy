package org.gradle.plugins.eclipseplugin

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.eclipseplugin.model.EclipsePluginDsl

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 10:01
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class EclipsePluginPlugin implements Plugin<Project>  {

    public final static String TASKNAME_CONFIGURE_BUILD = "configureBuild"
    public final static String TASKNAME_MIRROR_DEPENDENCIES = "mirrorDependencies"


    @Override
    void apply(Project project) {
        log.info ("Applying plugin ${getClass()} in project ${project.name}")

        project.plugins.apply(JavaPlugin) //We need for compile configuration

        EclipsePluginDsl plugindsl = project.extensions.create("eclipseplugin", EclipsePluginDsl, project)

        DefaultTask javaTask = project.tasks.findByName("compileJava")
        if (javaTask == null) throw new IllegalStateException("compileJava not available " + project.tasks.toListString())

        ConfigurePluginProjectTask configureBuildTask = project.tasks.create(type:ConfigurePluginProjectTask, name:TASKNAME_CONFIGURE_BUILD)
        javaTask.dependsOn configureBuildTask

        if (plugindsl.mirrorDependencies) {
          MirrorDependenciesTask mirrorDepsTask = project.tasks.create(type:MirrorDependenciesTask, name:TASKNAME_MIRROR_DEPENDENCIES)
          project.tasks.classes.dependsOn(mirrorDepsTask)
        }

        disableTestsBreakingTheBuild(project)
        configureProjectLayout(project)
    }


    public void disableTestsBreakingTheBuild (final Project project) {
        project.gradle.taskGraph.whenReady { taskGraph ->
            project.tasks.withType(Test).each { Test test ->
                test.ignoreFailures = true
                /**test.doFirst {  project.sourceSets.test.runtimeClasspath.getFiles().each { file ->
                    println "TestClasspath: ${file}"
                }} **/
            }
        }
    }

    public void configureProjectLayout (final Project project) {
        log.info("Configure projectlayout for project ${project.name}")
        project.sourceSets {
            main {
                java {
                    srcDirs = ["src", "src-gen"]

                }
                //compileClasspath = project.sourceSets.main.compileClasspath + project.sourceSets.srcgen.output
                resources { srcDirs = ["resources"] }
            }



            test {
                java { srcDirs = ["src"] }
                resources { srcDirs = ["test/resources"] }
            }
        }

        log.info(" - sourcesets : " + project.sourceSets.main.java.srcDirs)
    }
}
