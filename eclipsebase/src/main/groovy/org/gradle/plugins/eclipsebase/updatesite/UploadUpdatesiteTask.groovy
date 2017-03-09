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
 * Time: 13:44
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class UploadUpdatesiteTask extends DefaultTask {


    @TaskAction
    public void upload () {
        Eclipse eclipse = project.eclipsemodel
        EclipseBaseDsl basedsl = project.eclipsebase
        UpdatesiteDsl updatesite = basedsl.updatesite

        if (updatesite == null) {
            log.warn("No updatesite defined. Skip uploading updatesite")
            return
        }

        File localUpdatesite = eclipse.localUpdatesitePath

        println ("Uploading updatesite from ${localUpdatesite.absolutePath} to ${updatesite.path} on host ${updatesite.host}")

        ConfigurationContainer configurationContainer = project.configurations
        project.ant {
            taskdef(name: 'ftp', classname: 'org.apache.tools.ant.taskdefs.optional.net.FTP',classpath: configurationContainer.ftpAntTask.asPath)

            ftp(action: "del", remotedir: updatesite.path, server: updatesite.host, userid: updatesite.user, password: updatesite.pwd) {
                fileset(dir: ".", includes : '**/**')
            }

            ftp(action: "put", remotedir: updatesite.path, server: updatesite.host, userid: updatesite.user, password: updatesite.pwd, chmod:'644') {
                fileset(dir: localUpdatesite.absolutePath, includes : '**/**')
            }
        }

    }
}
