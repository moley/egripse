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

        File pluginProjectDir = new File (testprojectFlat, "org.eclipse.egripse.plugin")

        File buildDeps = new File (pluginProjectDir, "build/deps")
        Assert.assertTrue ("buildDeps path not created", buildDeps.exists())
        Assert.assertTrue ("not enough content in buildDeps path, expected at least 5 files", buildDeps.listFiles().length > 5)

    }

    private void checkUpdatesiteContent (final File rootpath)  {
      File pluginsPath = new File (rootpath, "plugins")
      Assert.assertTrue ("plugins path " + pluginsPath.absolutePath + " does not exist", pluginsPath.exists())
      for (File next: pluginsPath.listFiles()) {
        Assert.assertFalse("Found file " + next.absolutePath + " with qualifier in name", next.name.contains("qualifier"))
        Assert.assertFalse("Found file " + next.absolutePath + " with test in name", next.name.contains("test"))
      }

      File featuresPath = new File (rootpath, "features")
      Assert.assertTrue ("features path " + featuresPath.absolutePath + " does not exist", featuresPath.exists())
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

        checkUpdatesiteContent(new File (param.path, "build/newUpdatesiteContent"))
        checkUpdatesiteContent(new File (param.path, "build/updatesite"))
    }


}
