package org.gradle.plugins.eclipsebase.integrationtest

import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 12.10.13
 * Time: 21:43
 * To change this template use File | Settings | File Templates.
 */
class FlatProjectTest {

    private GradleLauncher launcher = new GradleLauncher()

    @Test
    public void start () {

        File path = launcher.getProjectPath("testprojects")
        File testprojectFlat = new File (path, "flat")

        GradleLauncherParam param = new GradleLauncherParam()
        param.path = testprojectFlat
        param.tasks = "clean build"

        GradleLauncherResult result = launcher.callGradleBuild(param)
        launcher.checkOutputForOK(result)
    }


}
