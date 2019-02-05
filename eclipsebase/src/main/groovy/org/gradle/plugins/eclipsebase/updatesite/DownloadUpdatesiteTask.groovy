package org.gradle.plugins.eclipsebase.updatesite

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.dsl.EclipseBaseDsl
import org.gradle.plugins.eclipsebase.dsl.UpdatesiteDsl
import org.gradle.plugins.eclipsebase.model.Eclipse

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 02.07.13
 * Time: 13:43
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class DownloadUpdatesiteTask extends DefaultTask{

    @TaskAction
    public void download () {

        Eclipse eclipse = project.eclipsemodel
        EclipseBaseDsl basedsl = project.eclipsebase

        File localUpdatesite = eclipse.localUpdatesitePath

        UpdatesiteDsl updatesite = basedsl.updatesite
        if (updatesite == null) {
            println "No updatesite defined. Skip downloading updatesite"
            return
        }

        if (updatesite.host == null) {
            println "No updatesite host defined. Skip downloading updatesite"
            return
        }

        if (updatesite.path == null) {
            println "No updatesite path defined. Skip downloading updatesite"
            return
        }

        println ("Downloading updatesite from host ${updatesite.host} to ${localUpdatesite.absolutePath}")

        ConfigurationContainer configurationContainer = project.configurations
        try {
            project.ant {
                taskdef(name: 'ftp', classname: 'org.apache.tools.ant.taskdefs.optional.net.FTP', classpath: configurationContainer.ftpAntTask.asPath)

                ftp(action: "mkdir", remotedir: updatesite.path, server: updatesite.host, userid: updatesite.user, password: updatesite.pwd)

                ftp(action: "get", remotedir: updatesite.path, server: updatesite.host, userid: updatesite.user, password: updatesite.pwd) {
                    fileset(dir: localUpdatesite.absolutePath)
                }
            }
        } catch (Exception e ) {
            println ("No previous content could be loaded from $updatesite.path. Please check if this is your first release on this path")
        }
    }

}
