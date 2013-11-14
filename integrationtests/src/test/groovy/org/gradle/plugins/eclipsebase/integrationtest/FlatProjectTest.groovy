package org.gradle.plugins.eclipsebase.integrationtest

import org.junit.Assert
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
    public void build () {

        File path = launcher.getProjectPath("testprojects")
        File testprojectFlat = new File (path, "flat")

        GradleLauncherParam param = new GradleLauncherParam()
        param.path = testprojectFlat
        param.withStacktrace = true
        param.tasks = "clean build --offline"

        GradleLauncherResult result = launcher.callGradleBuild(param)
        launcher.checkOutputForOK(result)
    }

    @Test
    public void updatesite () {

        File path = launcher.getProjectPath("testprojects")
        File testprojectFlat = new File (path, "flat")

        GradleLauncherParam param = new GradleLauncherParam()
        param.path = testprojectFlat
        param.tasks = "clean build updatesiteLocal --offline"
        param.withStacktrace = true

        GradleLauncherResult result = launcher.callGradleBuild(param)
        launcher.checkOutputForOK(result)

        File newUpdatesiteContent = new File (param.path, "build/newUpdatesiteContent/plugins")
        for (File next: newUpdatesiteContent.listFiles()) {
            Assert.assertFalse("Found file " + next.absolutePath + " with qualifier in name", next.name.contains("qualifier"))
        }

        File pluginsPath = new File (param.path, "build/updatesite/plugins")
        Assert.assertTrue ("plugins path " + pluginsPath.absolutePath + " does not exist", pluginsPath.exists())
        for (File next: pluginsPath.listFiles()) {
            Assert.assertFalse("Found file " + next.absolutePath + " with qualifier in name", next.name.contains("qualifier"))
        }

        File featuresPath = new File (param.path, "build/updatesite/features")
        Assert.assertTrue ("features path " + featuresPath.absolutePath + " does not exist", featuresPath.exists())

    }


}
