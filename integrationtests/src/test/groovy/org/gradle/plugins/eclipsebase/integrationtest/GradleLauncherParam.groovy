package org.gradle.plugins.eclipsebase.integrationtest

/**
 * parameter for the gradle launcher
 */
class GradleLauncherParam {

    /**
     * if set to true the gradle process is called for remote debugging
     */
    boolean enableRemoteDebugging = false

    /**
     * port to be used for remote debugging
     */
    int debugPort = 8000

    /**
     * adds --refreshDependencies if set to true
     */
    boolean refreshDependencies = false

    /**
     * if --stacktrace should be parameterized
     */
    boolean withStacktrace = true

    /**
     * debug level to be used for gradle launcher
     */
    GradleLauncherDebugLevel debugLevel = GradleLauncherDebugLevel.NORMAL

    /**
     * path were the gradle script is called
     */
    File path = new File("")

    /**
     * tasks that are called, different tasks are divided by space
     */
    String tasks = "clean build"

    /**
     * properties to be bound to gradle build
     */
    Map properties

    /**
     * buildscript filename
     */
    String buildscriptFile = "build.gradle"

    /**
     * {@inheritDoc}
     */
    public String toString () {
        return "Gradlebuildparameter: \n" +
                "- Remotedebugging: ${enableRemoteDebugging}\n"+
                "- Debugport      : ${debugPort}\n"+
                "- Refresh deps   : ${refreshDependencies}\n"+
                "- Debuglevel     : ${debugLevel.name()}\n"+
                "- Path           : ${path}\n"+
                "- Tasks          : ${tasks}\n" +
                "- Properties     : ${properties}\n" +
                "- Buildfile      : ${buildscriptFile}\n" +
                "- Stacktrace     : ${withStacktrace}"
    }


}
