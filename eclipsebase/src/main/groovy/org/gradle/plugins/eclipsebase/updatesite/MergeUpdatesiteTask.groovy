package org.gradle.plugins.eclipsebase.updatesite
import groovy.util.logging.Slf4j
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
@Slf4j
class MergeUpdatesiteTask extends RunExternalEclipseTask {


    @TaskAction
    public void exec () {
        Eclipse eclipse = project.eclipsemodel

        File updatesitePath = eclipse.localUpdatesitePath
        File updatesiteContentPath = eclipse.localUpdatesiteContentPath

        //Download eclipse executable
        Targetplatform externalEclipse = getExternalEclipse(project)

        log.info("Classpath updatesitemerge")
        for (File next: externalEclipse.updatesiteProgramsClasspath) {
            log.info("- Entry " + next.absolutePath)
        }

        println ("Merge udpatesite content from " + updatesiteContentPath.absolutePath + " to updatesite " + updatesitePath.absolutePath)

        workingDir updatesiteContentPath
        main 'org.eclipse.core.launcher.Main'
        classpath externalEclipse.updatesiteProgramsClasspath
        jvmArgs '-Xms40m'
        jvmArgs '-Xmx900m'
        jvmArgs '-XX:MaxPermSize=512m'

        //args '-console'
        args '-consolelog'
        args '-application', 'org.eclipse.equinox.p2.publisher.FeaturesAndBundlesPublisher'
        args '-metadataRepository', 'file:' + updatesitePath.absolutePath + '/'
        args '-append'
        args '-artifactRepository', 'file:'  + updatesitePath.absolutePath + '/'
        args '-source ' + updatesiteContentPath.absolutePath
        //args '-compress'
        args '-publishArtifacts'


        args (args)
        setErrorOutput(new ByteArrayOutputStream())
        setStandardOutput(new ByteArrayOutputStream())

        println ("Commandline: " + toString(commandLine))

        super.exec()
        println ("ErrorOutput: " + errorOutput.toString())
        println ("StandardOutput: " + standardOutput.toString())
    }

    private String toString (final List<String> list) {
        String asString = ""
        for (String next: list) {
            asString += " " + next
        }

        return asString.trim()
    }
}
