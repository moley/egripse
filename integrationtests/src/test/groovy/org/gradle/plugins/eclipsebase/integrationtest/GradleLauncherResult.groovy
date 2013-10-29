package org.gradle.plugins.eclipsebase.integrationtest

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 05.08.13
 * Time: 10:04
 * To change this template use File | Settings | File Templates.
 */
class GradleLauncherResult {

    /**
     * output of process
     */
    List<String> output

    /**
     * timestamp of process beginning
     */

    long beginning


    /**
     * timestamp of process finished
     */
    long finished

    /**
     * constructor
     * @param output        output
     * @param beginning     timestamp of beginning
     * @param finished      timestamp of finishing
     */
    public GradleLauncherResult (final List<String> output, final long beginning, final long finished) {
        this.output = output
        this.beginning = beginning
        this.finished = finished
    }




}
