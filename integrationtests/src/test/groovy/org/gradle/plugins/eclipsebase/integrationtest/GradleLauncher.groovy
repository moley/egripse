package org.gradle.plugins.eclipsebase.integrationtest

import com.google.common.io.Files
import groovy.util.logging.Log4j
import org.apache.commons.io.FileUtils
import org.gradle.api.GradleException
import org.junit.Assert

/**
 * launches a gradle build, e.g. for integrationtests
 */
@Log4j
class GradleLauncher {

    String OS = System.getProperty("os.name")
    boolean isWindows = OS.startsWith("Windows")


    /**
     * checks if a build was ok
     * @param output  output (from returnobject of method callGradleBuild)
     */
    public void checkOutputForOK (final GradleLauncherResult result) {
        println result.output
        Assert.assertTrue (result.asSplittedString(), result.output.contains("BUILD SUCCESSFUL"))
    }

    /**
     * filters asci sequences
     * @param withAscii string with asci sequences
     * @return string without ascii sequences
     */
    private String filterAscii (final String withAscii) {
        return withAscii.replace("[22;31m", "").replace("[0m", "")
    }

    /**
     *
     * @param output
     * @param errorCode (from returnobject of method callGradleBuild)
     */
    public void checkOutputForError (final GradleLauncherResult result, final String errorCode, final String context, final String message) {
        Assert.assertTrue (result.output.contains("BUILD FAILED"))

        int errorline = -1

        for (int i = 0; i < result.output.size(); i++) {
            String nextLine = result.output.get(i)
            if (nextLine.trim().startsWith("Error") && nextLine.contains(errorCode)) {
                errorline = i
                break
            }
        }


        Assert.assertTrue ("No error with errorcode " + errorCode + " was found in output", errorline > 0)

        errorline ++

        if (context != null) {
            Assert.assertTrue("Context was wrong (expected: " + context + ", real: " + result.output.get(errorline).substring(12).trim() + ")" , result.output.get(errorline).substring(12).trim().contains(context))
            errorline ++
        }

        if (message != null) {
            Assert.assertTrue("Message was wrong (expected: " + context + ", real: " + result.output.get(errorline).substring(12).trim() + ")", result.output.get(errorline).substring(12).trim().contains(message))
        }
    }

    /**
     * Gets path to a gradleplugins-module
     *
     * @param project name of the project, e.g. 'marvin'
     * @return path
     */
    public File getProjectPath(final String project) {

        File pathAsPath = new File(project)
        if (!pathAsPath.exists()) {
            org.gradle.plugins.eclipsebase.integrationtest.GradleLauncher.log.info("Path " + pathAsPath.absolutePath + " doesn't exist, look up in parent")
            pathAsPath = new File(new File("").absoluteFile.parentFile, project)
        }
        else
            org.gradle.plugins.eclipsebase.integrationtest.GradleLauncher.log.info("Using projectpath " + pathAsPath.absolutePath)

        if (!pathAsPath.exists())
            throw new IllegalStateException("Path " + pathAsPath.absolutePath + " not found in project.")

        return pathAsPath
    }

    /**
     * Calls gradle with the given task in the given directory
     * Does NOT work without patching the gradle start script
     * Can be used for tests
     *
     * @param param parameter for the build run
     * @return result
     */
    public GradleLauncherResult callGradleBuild(final GradleLauncherParam param) throws GradleException {
        File pathAsPath = param.path
        String gradleHome = System.getenv("GRADLE_HOME")
        if (gradleHome == null) throw new IllegalStateException("Environment Variable GRADLE_HOME is not set")



        if (param.copyFrom != null) {
            File root = new File (getProjectPath("integrationtests").parentFile, "tmp")
            pathAsPath = new File (root, Files.createTempDir().name)
            println ("Using temporaer path " + pathAsPath.absolutePath)
            FileUtils.copyDirectory(param.copyFrom, pathAsPath)
            if (param.buildscriptFile != null) {
              FileUtils.copyFile(new File (param.copyFrom, param.buildscriptFile), new File (pathAsPath, "build.gradle"))
              param.buildscriptFile = param.DEFAULT_BUILDSCRIPT
            }
            param.path = pathAsPath
        }


        File propFiles = new File(pathAsPath, "project.properties")
        if (!propFiles.exists())
            Assert.assertTrue(propFiles.createNewFile())

        def env = []
        System.getenv().each { k, v ->
            env << "$k=$v"
        }

        long before = -1
        long after = -1


        File selectedGradleHome = gradleHome != null ? new File (gradleHome) : pathAsPath

        env << "GRADLE_HOME=" + selectedGradleHome.absolutePath
        env << "CLASSPATH=" //because log4j is on this variable from intellij

        env.each { println it }

        def returnMessages = []
        try {
            def commands = []
            def gradlecommands = []


            if (isWindows)
                commands << (selectedGradleHome.absolutePath + File.separator + "bin" + File.separator + "gradle.bat")
            else
                commands << (selectedGradleHome.absolutePath + File.separator + "bin" + File.separator + "gradle")

            if (param.enableRemoteDebugging) {
                gradlecommands << "debug"
                gradlecommands << param.debugPort
            }

            gradlecommands.addAll(param.tasks.split(" "))

            if (param.withStacktrace)
                gradlecommands << "--stacktrace"

            if (param.debugLevel == GradleLauncherDebugLevel.INFO) {
                gradlecommands << "--info"
            } else if ( param.debugLevel == GradleLauncherDebugLevel.DEBUG) {
                gradlecommands << "--debug"
            }

            if (param.refreshDependencies) {
                gradlecommands << "--refresh-dependencies"
            }

            gradlecommands << "-b"
            gradlecommands << param.buildscriptFile

            if (param.properties != null) {
                param.properties.each { k, v ->
                    gradlecommands << "-P$k=$v"
                }
            }


            commands.addAll(gradlecommands)

            org.gradle.plugins.eclipsebase.integrationtest.GradleLauncher.log.info ("Commands:" + commands + "- Current path: " + new File ("").absolutePath + "- GRADLE_HOME: " + selectedGradleHome.absolutePath)
            org.gradle.plugins.eclipsebase.integrationtest.GradleLauncher.log.info (param.toString())


            before = System.currentTimeMillis()
            def process = commands.execute(env, pathAsPath)

            def inThread = Thread.start {
                process.in.eachLine {
                    String nextOutput = filterAscii(it)
                    returnMessages << nextOutput
                    println(nextOutput)
                }
            }

            def errThread = Thread.start {
                process.err.eachLine {
                    String nextOutput = filterAscii(it)
                    returnMessages << nextOutput
                    println(nextOutput)
                }
            }

            inThread.join()
            errThread.join()

            process.waitFor()
        } catch (Exception e) {
            throw new GradleException(e.toString() + "(GRADLE_HOME=" + selectedGradleHome.absolutePath + ")", e);
        } finally {
            after = System.currentTimeMillis()
        }
        return new GradleLauncherResult(returnMessages, before, after)
    }
}
