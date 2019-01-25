package org.gradle.plugins.eclipsebase.integrationtest

import org.gradle.testkit.runner.GradleRunner
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.Assert
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


    @Test
    public void completeBuild () {

        ProjectConnection connection = GradleConnector.newConnector().forProjectDirectory(new File("someProjectFolder")).connect();

        try {
            connection.newBuild().forTasks("tasks").run();
        } finally {
            connection.close();
        }

    }

    @Test
    public void eclipse () {
        println GradleRunner.create().withProjectDir(testprojectFlat).withArguments(['eclipse']).build().output



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
        println GradleRunner.create().withProjectDir(testprojectFlat).withArguments(["-b withParentsFirst.gradle", "clean", "build"]).build().output

        File pluginProjectDir = new File (testprojectFlat, "org.eclipse.egripse.plugin")

        File buildDeps = new File (pluginProjectDir, "build/deps")
        Assert.assertTrue ("buildDeps path not created", buildDeps.exists())
        Assert.assertTrue ("not enough content in buildDeps path, expected at least 5 files", buildDeps.listFiles().length > 5)

    }

    @Test
    public void build () {
        println GradleRunner.create().withProjectDir(testprojectFlat).withArguments(["clean", "build"]).build().output


        File pluginProjectDir = new File (testprojectFlat, "org.eclipse.egripse.plugin")

        File buildDeps = new File (pluginProjectDir, "build/deps")
        Assert.assertTrue ("buildDeps path not created", buildDeps.exists())
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
        println GradleRunner.create().withProjectDir(testprojectFlat).withArguments(["clean", "build", "updatesiteLocal"]).build().output

        checkUpdatesiteContent(new File (param.path, "build/newUpdatesiteContent"))
        checkUpdatesiteContent(new File (param.path, "build/updatesite"))
    }

    @Test
    public void uitests () {
        println GradleRunner.create().withProjectDir(testprojectFlat).withArguments(["clean", "uitest"]).build().output

        File pluginProjectDir = new File (testprojectFlat, "org.eclipse.egripse.plugin.test")

        File buildReportsUi = new File (pluginProjectDir, "build/test-results-ui")
        File uiXml = new File (buildReportsUi, 'TEST-org.eclipse.egripse.plugin.test.uitest.TestUi.xml')
        Assert.assertTrue ("Report-Xml " + uiXml + " does not exist", uiXml.exists())
    }


}
