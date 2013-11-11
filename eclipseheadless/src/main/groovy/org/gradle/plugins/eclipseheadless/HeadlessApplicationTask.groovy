package org.gradle.plugins.eclipseheadless

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.model.Eclipse

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 09.11.13
 * Time: 22:19
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class HeadlessApplicationTask extends DefaultTask {

    /**
     * name of application
     */
    String applicationname

    /**
     * if set we copy the built plugin to temporar targetplatform
     */
    String feature





    @TaskAction
    void startHeadlessApplication () {
        Eclipse eclipseModel = project.rootProject.extensions.eclipsemodel

        File headlessRootPath = project.file("build/headless/" + name)
        log.info("Executing application " + applicationname + " in path " + headlessRootPath.absolutePath)

        File copyFromPath = new File (eclipseModel.explodedTargetplatform, "eclipse")

        project.copy {
            from (copyFromPath)
            into(headlessRootPath)
        }

        File equinoxLauncherJar = new File (headlessRootPath, "plugins/org.eclipse.equinox.launcher_1.3.0.v20130327-1440.jar") //TODO search for


        project.exec {
            workingDir headlessRootPath
            commandLine "java",
                        "-version"
                        //"-jar ${equinoxLauncherJar.absolutePath}",
                        //"-consoleLog",
                        //"-console",
                        //"-nosplash",
                        //"-application ${applicationname}"

        }


    }
}
