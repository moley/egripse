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
    protected String applicationname

    /**
     * name of workspace, defaults to taskname
     */
    protected String workspacename

    protected Collection<String> parameters = new ArrayList<String>()



    public void workspacename (final String workspacename) {
        log.info("set workspacename " + workspacename)
        this.workspacename = workspacename
    }

    public void applicationname (final String name) {
        log.info("Set applicationname " + name)
        this.applicationname = name
    }

    public void parameter (final String nextParam) {
        log.info("add parameter " + nextParam)
        parameters.add(nextParam)
    }



    @TaskAction
    void startHeadlessApplication() {
        println("Starting...")
        Eclipse eclipseModel = project.rootProject.extensions.eclipsemodel

        if (workspacename == null)
            workspacename = name

        File headlessRootPath = project.file("build/headless/" + workspacename)
        log.info("Executing application " + applicationname + " in path " + headlessRootPath.absolutePath)

        File copyFromPath = new File(eclipseModel.explodedTargetplatform, "eclipse")


        if (! headlessRootPath.exists()) {
          println ("Copying workspace to ${headlessRootPath.absolutePath}...")
          project.copy {
           from (copyFromPath)
           into(headlessRootPath)
          }
        }
        else
            log.info("Workspace ${headlessRootPath.absolutePath} exists, skip copying")

        File equinoxLauncherJar = new File(headlessRootPath, "plugins/org.eclipse.equinox.launcher_1.3.0.v20130327-1440.jar") //TODO search for

        println("Executing ...")
        log.info("Starting headless application ${applicationname} in workingdir ${headlessRootPath}")

        ByteArrayOutputStream bos = new ByteArrayOutputStream()
        ByteArrayOutputStream eos = new ByteArrayOutputStream()

        Collection<String> arguments = new ArrayList<String>()
        arguments.add("java")
        arguments.add('-jar')
        arguments.add(equinoxLauncherJar.absolutePath)
        arguments.add('-debug')



        arguments.add('-consolelog')
        arguments.add('-nosplash')
        arguments.add('-application')
        if (applicationname == null)
            throw new IllegalStateException("You did not configure an applicationname for task " + name)
        arguments.add(applicationname)

        if (parameters != null)
          arguments.addAll(parameters)


        String operatingsystem = System.getProperty("os.name")
        if (operatingsystem.contains("Mac")) {
            arguments.add("-vmargs")
            arguments.add("-XstartOnFirstThread")
        }



        println ("Arguments: " + arguments)





        try {

            project.exec {
                standardOutput = bos
                errorOutput = eos
                workingDir = headlessRootPath
                commandLine = arguments
            }
        } catch (Exception e) {

            println("Output: " + bos.toString())
            println("Error : " + eos.toString())
            throw e

        }


    }
}
