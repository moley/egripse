package org.gradle.plugins.eclipsesurefire

import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.EclipsePlugin
import org.gradle.plugins.eclipseheadless.HeadlessApplicationTask
import org.gradle.process.ExecResult

import java.nio.file.Files

/**
 * Created by OleyMa on 21.11.14.
 */
@Slf4j
class StartUiTestsTask extends HeadlessApplicationTask {



    private String testApplication = "org.eclipse.ui.ide.workbench";

    Properties props = new Properties()

    private EclipsePlugin plugin
    private UITestDsl uiTestDsl
    private Eclipse eclipse


    protected File getSurefireProperties () {
        return new File (getHeadlessRootPath(), 'surefire.properties')
    }

    public installBundleAndDependencies (EclipsePlugin plugin) {
        log.info("Install plugin $plugin.bundleID in runtime")

        File libPath = new File(plugin.originPath, 'build/libs')
        if (libPath.exists()) {

            File latest = libPath.listFiles()[0]
            for (File next : libPath.listFiles()) {
                if (next.absolutePath > latest.absolutePath)
                    latest = next
            }

            log.info("Latest file in path $libPath.absolutePath is $latest.absolutePath")

            File to = new File(headlessRootPath, 'plugins')

            log.info("Installing plugin from " + latest + " to " + to)
            project.copy {
                from(latest)
                into(to)
            }
        }
        else
            log.warn("Libpath $libPath.absolutePath does not exist")

    }

    @TaskAction
    ExecResult startHeadlessApplication() {
        log.info("Start task " + getClass().getName() + " to execute ui tests")
        applicationname = "org.eclipse.tycho.surefire.osgibooter.uitest";


        uiTestDsl = project.extensions.findByName("uitest")

        debugEnabled = uiTestDsl.showDebugInfo

        eclipse = project.rootProject.extensions.findByName("eclipsemodel")
        plugin = eclipse.workspace.findPluginByPath(project.projectDir)

        if (uiTestDsl.debugPort > 0) {
            jvmArguments.add("-Xdebug")
            jvmArguments.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=$uiTestDsl.debugPort")
        }

        jvmArguments.add("-Dosgi.noShutdown=false")

        parameters.add("-testproperties")
        parameters.add(surefireProperties.absolutePath)

        parameters.add("-testApplication")
        parameters.add(testApplication)

        parameters.add("-nouithread")

        startEclipse = true


        ExecResult execResult = super.startHeadlessApplication()
        int result = execResult.exitValue

        switch (result) {
            case 0:
                log.info("All tests passed!");
                break;

            case 200: /* see AbstractUITestApplication */
                    throw new GradleException("Could not find application \"" + testApplication
                            + "\" in the test runtime. Make sure that the test runtime includes the bundle "
                            + "which defines this application.");

            case 254/* RunResult.NO_TESTS */:
                String message = "No tests found.";
                if (uiTestDsl != null && uiTestDsl.failIfNoTests) {
                    throw new GradleException(message);
                } else {
                    log.warn(message);
                }
                break;

            case 255/* RunResult.FAILURE */:
                String errorMessage = "There are test failures.\n\nPlease refer to " + reportsDirectory
                + " for the individual test results.";
                if (uiTestDsl != null && uiTestDsl.testFailureIgnore) {
                    log.error(errorMessage);
                } else {
                    throw new GradleException(errorMessage);
                }
                break;

            default:
                throw new GradleException("An unexpected error occured while launching the test runtime (return code "
                        + result + "). See log for details.");
        }

        return execResult

    }


    protected void afterPlatformExists () {

        //install osgibooter plugin
        project.rootProject.buildscript.configurations.classpath.each {
            log.info("Check additional surefire dependency")

            if (it.name.contains("surefire") && ! it.name.startsWith("eclipsesurefire-")) {

                File toPlugin = new File(pluginsPath, it.name)
                if (toPlugin.exists()) {
                    if (! toPlugin.delete())
                        throw new IllegalStateException("surefire file $toPlugin.absolutePath exists, remove it");
                }

                log.info("Copy $it.absolutePath  to headless installation $toPlugin.absolutePath")
                Files.copy(it.toPath(), toPlugin.toPath())
            }
            else
                log.info("Do not copy $it.absolutePath")

        }

        //install plugin and deps
        eclipse.workspace.plugins.each {
            installBundleAndDependencies(it)

        }


        //http://git.eclipse.org/c/tycho/org.eclipse.tycho.git/tree/tycho-surefire/tycho-surefire-plugin/src/main/java/org/eclipse/tycho/surefire/TestMojo.java
        SourceSet sourcesetMain = project.sourceSets.main

        props.clear()
        setAndLogProperty(props, "testpluginname", plugin.getBundleID())
        setAndLogProperty(props, "testclassesdirectory", sourcesetMain.output.classesDir.absolutePath)
        setAndLogProperty(props, "reportsdirectory", "build/reports")
        setAndLogProperty(props, "redirectTestOutputToFile", "true") //TODO make configurable
        setAndLogProperty(props, "failifnotests", uiTestDsl.failIfNoTests.toString())
        setAndLogProperty(props, "runOrder", "filesystem") //TODO make configurable
        setAndLogProperty(props, "testprovider", "org.apache.maven.surefire.junit4.JUnit4Provider") //TODO make configurable
        setAndLogProperty(props, "excludes", uiTestDsl.excludesAsString)
        setAndLogProperty(props, "includes", uiTestDsl.includesAsString)


        //TODO add properties here
        props.store(new FileOutputStream(surefireProperties), "created by egripse plugin")

    }

    public void setAndLogProperty (final Properties props, final String key, String value) {
        log.info("Setting property " + key + " to <" + value + ">")
        props.setProperty(key, value)
    }




    //org.eclipse.tycho.surefire.osgibooter.headlesstest   ->Headlesstest


}
