package org.gradle.plugins.eclipsebase.integrationtest

import org.apache.commons.io.FileUtils
import org.gradle.testkit.runner.GradleRunner
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 12.10.13
 * Time: 21:43
 * To change this template use File | Settings | File Templates.
 */
class FlatProjectTest extends AbstractIntegrationTest {

    File path = getProjectPath("testprojects")
    File testprojectFlat = new File (path, "flat")


    private clearProject (final File projectpath) {
        FileUtils.deleteDirectory(new File (projectpath, 'build'))
        FileUtils.deleteDirectory(new File (projectpath, '.gradle'))
    }

    @Test
    public void tasks () {
        GradleRunner.create().withProjectDir(testprojectFlat).withArguments(['tasks', '-s']).build().output
    }

    @Test
    public void eclipse () {
        clearProject(testprojectFlat)
        println GradleRunner.create().withProjectDir(testprojectFlat).withArguments(['eclipse', '-s']).build().output

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
    public void build () {
        clearProject(testprojectFlat)
        println GradleRunner.create().withProjectDir(testprojectFlat).withArguments(['build', '-s']).build().output


        File pluginProjectDir = new File (testprojectFlat, "org.eclipse.egripse.plugin")

        File buildDeps = new File (pluginProjectDir, "build/deps")
        Assert.assertTrue ("buildDeps path $buildDeps.absolutePath not created", buildDeps.exists())
        Assert.assertTrue ("not enough content in buildDeps path, expected at least 5 files", buildDeps.listFiles().length > 5)

        File buildReportsUi = new File (pluginProjectDir, "build/test-results")
        Assert.assertTrue ("buildDeps path not created", buildReportsUi.exists())

        File standaloneXml = new File (buildReportsUi, 'TEST-org.eclipse.egripse.plugin.test.TestStandalone.xml')
        Assert.assertTrue ("Report-Xml " + standaloneXml + " does not exist", standaloneXml.exists())
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
        clearProject(testprojectFlat)
        println GradleRunner.create().withProjectDir(testprojectFlat).withArguments(['build', 'updatesite', "-s"]).build().output

        checkUpdatesiteContent(new File (testprojectFlat, "build/newUpdatesiteContent"))
        checkUpdatesiteContent(new File (testprojectFlat, "build/updatesite"))
    }

    @Test@Ignore
    public void uitests () {
        println GradleRunner.create().withProjectDir(testprojectFlat).withArguments(["clean", "uitest"]).build().output

        File pluginProjectDir = new File (testprojectFlat, "org.eclipse.egripse.plugin.test")

        File buildReportsUi = new File (pluginProjectDir, "build/test-results-ui")
        File uiXml = new File (buildReportsUi, 'TEST-org.eclipse.egripse.plugin.test.uitest.TestUi.xml')
        Assert.assertTrue ("Report-Xml " + uiXml + " does not exist", uiXml.exists())
    }


}
