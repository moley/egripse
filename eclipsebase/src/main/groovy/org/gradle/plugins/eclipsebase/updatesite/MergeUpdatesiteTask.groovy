package org.gradle.plugins.eclipsebase.updatesite

import groovy.util.logging.Slf4j
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.dsl.EclipseBaseDsl
import org.gradle.plugins.eclipsebase.model.Eclipse

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 02.07.13
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class MergeUpdatesiteTask extends JavaExec{


    @TaskAction
    public void exec () {

        Eclipse eclipse = project.eclipsemodel

        File updatesitePath = project.file("build/updatesite")

        log.info("Classpath updatesitemerge")
        for (File next: eclipse.targetplatformModel.updatesiteProgramsClasspath) {
            log.info("- Entry " + next.absolutePath)
        }

        workingDir 'build/newUpdatesiteContent'
        main 'org.eclipse.core.launcher.Main'
        classpath eclipse.targetplatformModel.updatesiteProgramsClasspath
        jvmArgs '-Xms40m'
        jvmArgs '-Xmx900m'
        jvmArgs '-XX:MaxPermSize=512m'

        args '-application', 'org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher'
        args '-metadataRepository', 'file:' + updatesitePath.absolutePath
        args '-append'
        args '-artifactRepository', 'file:'  + updatesitePath.absolutePath
        args '-source build/newUpdatesiteContent'
        args '-compress'
        args '-publishArtifacts'
        args '-console'
        args '-consoleLog'
        args (args)

        super.exec()
    }
}
