package org.gradle.plugins.eclipseheadless

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.process.ExecResult

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

    protected Collection<String> memparameters = new ArrayList<String>()

    protected Collection<String> parameters = new ArrayList<String>()

    /**
     * true:  starting eclipse itself,
     * false: starting equinox launcher only
     */
    protected startEclipse = false

    Collection<String> arguments = new ArrayList<String>()
    Collection<String> jvmArguments = new ArrayList<String>()

    protected boolean debugEnabled = false





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

    public void xmx (final String xmx) {
        memparameters.add("-Xmx${xmx}")
    }

    public void maxPermsize (final String maxpermsize) {
        memparameters.add("-XX:MaxPermSize=${maxpermsize}")
    }

    /**
     * searches the equinox-launcher in the given path
     * @param inPath  path
     * @return one launcher jar
     */
    public File findEquinoxLauncher (final File inPath) {
        List<File> foundFiles = inPath.listFiles().findAll {println it;it.name.startsWith('org.eclipse.equinox.launcher_')}
        if  (foundFiles.size() == 0)
            throw new IllegalStateException("No org.eclipse.equinox.launcher - jar found in path $inPath.absolutePath")
        if  (foundFiles.size() > 1)
            throw new IllegalStateException("Multiple org.eclipse.equinox.launcher - jars found in path $inPath.absolutePath, only one is allowed")

        return foundFiles.first()
    }

    protected File getHeadlessRootPath () {
        return project.file("build/headless/" + getWorkspacename())
    }

    protected String getWorkspacename () {
        if (workspacename == null)
            workspacename = name
        return workspacename
    }

    protected File getPluginsPath () {
        return new File (headlessRootPath, 'plugins')
    }

    /**
     * Lifecycle callback
     */
    protected void afterPlatformExists () {

    }

    @TaskAction
    ExecResult startHeadlessApplication() {
        println("Starting...")
        Eclipse eclipseModel = project.rootProject.extensions.eclipsemodel

        boolean isMac = System.getProperty("os.name").contains("Mac")



        File headlessRootPath = getHeadlessRootPath()
        log.info("Executing application " + applicationname + " in path " + headlessRootPath.absolutePath)

        File copyFromPath = new File(eclipseModel.explodedTargetplatform, "eclipse")


        if (!headlessRootPath.exists()) {
            println("Copying workspace to ${headlessRootPath.absolutePath}...")
            project.copy {
                from(copyFromPath)
                into(headlessRootPath)
            }
        } else
            log.info("Workspace ${headlessRootPath.absolutePath} exists, skip copying")

        File pluginPath = new File(headlessRootPath, 'plugins')
        File equinoxLauncherJar = findEquinoxLauncher(pluginPath)

        afterPlatformExists()

        println("Executing ...")
        log.info("Starting headless application ${applicationname} in workingdir ${headlessRootPath}")





        arguments.add(equinoxLauncherJar.absolutePath)

        if (memparameters != null)
            arguments.addAll(memparameters)

        if (isMac) {
            jvmArguments.add("-XstartOnFirstThread")

        }

        if (debugEnabled)          {
          arguments.add('-debug')
          arguments.add('-consoleLog')
        }



        //application parameter
        arguments.add('-application')
        if (applicationname == null)
            throw new IllegalStateException("You did not configure an applicationname for task " + name)
        arguments.add(applicationname)

        if (parameters != null)
          arguments.addAll(parameters)

        File workspace = project.file('build/workspace' + System.currentTimeMillis())

        arguments.add('-data')
        arguments.add(workspace.absolutePath)
        arguments.add('-config')
        arguments.add(new File (workspace, 'configuration').absolutePath)
        arguments.add('-nosplash')

        println ("Arguments: " + arguments)
        println ("jvmArguments: " + jvmArguments)

        try {

            ExecResult result = project.javaexec {
                main '-jar'
                args arguments
                jvmArgs jvmArguments
            }

            return result;
        } catch (Exception e) {
            throw e
        }


    }
}
