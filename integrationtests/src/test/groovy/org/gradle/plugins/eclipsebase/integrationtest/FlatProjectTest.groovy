package org.gradle.plugins.eclipsebase.integrationtest

import org.apache.commons.io.FileUtils
import org.junit.After
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

    File path = launcher.getProjectPath("testprojects")
    File testprojectFlat = new File (path, "flat")

    GradleLauncherParam param

    @After
    public void after () {
        if (param.copyFrom != null && param.path.absolutePath.contains("tmp"))
            FileUtils.deleteDirectory(param.path)

    }

    @Test
    public void eclipse () {
        param = new GradleLauncherParam()
        param.copyFrom = testprojectFlat
        param.withStacktrace = true
        param.tasks = "eclipse"

        GradleLauncherResult result = launcher.callGradleBuild(param)
        launcher.checkOutputForOK(result)

        File pluginProjectDir = new File (testprojectFlat, "org.eclipse.egripse.plugin")
        String dotProjectPlugin = new File (pluginProjectDir, ".project").text
        Assert.assertTrue ("No manifest builder added", dotProjectPlugin.contains("<name>org.eclipse.pde.ManifestBuilder</name>"))
        Assert.assertTrue ("No schema builder added", dotProjectPlugin.contains("<name>org.eclipse.pde.SchemaBuilder</name>"))
        Assert.assertTrue ("No java builder added", dotProjectPlugin.contains("<name>org.eclipse.jdt.core.javabuilder</name>"))

        Assert.assertTrue ("No PluginNature added", dotProjectPlugin.contains("<nature>org.eclipse.pde.PluginNature</nature>"))
        Assert.assertTrue ("No JavaNature added", dotProjectPlugin.contains("<nature>org.eclipse.jdt.core.javanature</nature>"))

        File featureProjectDir = new File (testprojectFlat, "org.eclipse.egripse.feature")
        String dotProjectFeature = new File (featureProjectDir, ".project").text
        Assert.assertTrue ("No FeatureBuilder added", dotProjectFeature.contains("<name>org.eclipse.pde.FeatureBuilder</name>"))
        Assert.assertTrue ("No FeatureNature added", dotProjectFeature.contains("<nature>org.eclipse.pde.FeatureNature</nature>"))
    }

    @Test
    public void buildParentsFirst () {
        param = new GradleLauncherParam()
        param.copyFrom = testprojectFlat
        param.withStacktrace = true
        param.buildscriptFile = "withParentsFirst.gradle"
        param.tasks = "clean build"

        GradleLauncherResult result = launcher.callGradleBuild(param)
        launcher.checkOutputForOK(result)

        File pluginProjectDir = new File (testprojectFlat, "org.eclipse.egripse.plugin")

        File buildDeps = new File (pluginProjectDir, "build/deps")
        Assert.assertTrue ("buildDeps path not created", buildDeps.exists())
        Assert.assertTrue ("not enough content in buildDeps path, expected at least 5 files", buildDeps.listFiles().length > 5)

    }

    @Test
    public void build () {
        param = new GradleLauncherParam()
        param.path = testprojectFlat
        param.withStacktrace = true
        param.tasks = "clean build"

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
        param = new GradleLauncherParam()
        param.path = testprojectFlat
        param.tasks = "clean build updatesiteLocal"
        param.withStacktrace = true

        GradleLauncherResult result = launcher.callGradleBuild(param)
        launcher.checkOutputForOK(result)

        checkUpdatesiteContent(new File (param.path, "build/newUpdatesiteContent"))
        checkUpdatesiteContent(new File (param.path, "build/updatesite"))
    }


}
