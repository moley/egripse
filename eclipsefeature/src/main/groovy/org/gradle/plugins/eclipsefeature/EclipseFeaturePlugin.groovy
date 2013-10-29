package org.gradle.plugins.eclipsefeature

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 03.07.13
 * Time: 08:06
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class EclipseFeaturePlugin implements Plugin<Project>  {



    @Override
    void apply(Project project) {

        log.info ("Applying plugin ${getClass()} in project ${project.name}")

        project.plugins.apply(JavaPlugin) //We need for compile configuration

        DefaultTask javaTask = project.tasks.findByName("compileJava")

        ConfigureFeatureProjectTask configureBuildTask = project.tasks.create(type:ConfigureFeatureProjectTask, name:"configureBuild")
        javaTask.dependsOn configureBuildTask

    }
}
