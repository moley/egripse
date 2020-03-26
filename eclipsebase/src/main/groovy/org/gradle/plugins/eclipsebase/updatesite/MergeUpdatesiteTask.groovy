package org.gradle.plugins.eclipsebase.updatesite


import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.Targetplatform

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 02.07.13
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
class MergeUpdatesiteTask extends Exec {

    @TaskAction
    public void exec () {
        Eclipse eclipse = project.eclipsemodel

        File updatesitePath = eclipse.localUpdatesitePath
        File updatesiteContentPath = eclipse.localUpdatesiteContentPath

        //Download eclipse executable
        Targetplatform externalEclipse = eclipse.targetplatformModel

        project.logger.lifecycle("Merge udpatesite content from " + updatesiteContentPath.absolutePath + " to updatesite " + updatesitePath.absolutePath)

        workingDir updatesiteContentPath
        executable(externalEclipse.executableEclipse(project).absolutePath)

        args '-Xmx900m'

        //args '-console'
        args '-application', 'org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher'
        args '-metadataRepository', 'file:' + updatesitePath.absolutePath + '/'
        args '-append'
        args '-artifactRepository', 'file:'  + updatesitePath.absolutePath + '/'
        args '-source', updatesiteContentPath.absolutePath
        //args '-compress'
        args '-publishArtifacts'
        //args '-console'
        //args '-consoleLog'
        args '-nosplash'

        project.logger.info("Call " + String.join(" ", commandLine))
        super.exec()
    }
}
