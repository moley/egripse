package org.gradle.plugins.eclipsebase.updatesite

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.tasks.TaskAction
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

        Eclipse eclipse = project.eclipsebase
        File localUpdatesite = eclipse.localUpdatesitePath

        UpdatesiteDsl updatesite = eclipse.updatesite
        if (updatesite == null) {
            log.warn("No updatesite defined, skipping upload updatesite")
            return
        }

        log.info("Downloading updatesite to ${localUpdatesite.absolutePath}")
        ConfigurationContainer configurationContainer = project.configurations
        project.ant {
            taskdef(name: 'ftp', classname: 'org.apache.tools.ant.taskdefs.optional.net.FTP', classpath: configurationContainer.ftpAntTask.asPath)

            ftp(action: "mkdir", remotedir: updatesite.path, server: updatesite.host, userid: updatesite.user, password: updatesite.pwd)

            ftp(action: "get", remotedir: updatesite.path, server: updatesite.host, userid: updatesite.user, password: updatesite.pwd) {
                fileset(dir: localUpdatesite.absolutePath)
            }
        }
    }

}
