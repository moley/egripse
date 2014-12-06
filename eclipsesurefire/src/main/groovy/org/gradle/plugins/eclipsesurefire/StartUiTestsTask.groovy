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

    /**private EquinoxInstallation createEclipseInstallation()  {
        DependencyResolver platformResolver = dependencyResolverLocator.lookupDependencyResolver(project);
        final List<Dependency> extraDependencies = getExtraDependencies();
        List<ReactorProject> reactorProjects = getReactorProjects();

        final DependencyResolverConfiguration resolverConfiguration = new DependencyResolverConfiguration() {
            @Override
            public OptionalResolutionAction getOptionalResolutionAction() {
                return OptionalResolutionAction.IGNORE;
            }

            @Override
            public List<Dependency> getExtraRequirements() {
                return extraDependencies;
            }
        };


        if (testRuntimeArtifacts == null) {
            throw new GradleException("Cannot determinate build target platform location -- not executing tests");
        }

        work.mkdirs();

        EquinoxInstallationDescription testRuntime = new DefaultEquinoxInstallationDescription();
        testRuntime.addBundlesToExplode(getBundlesToExplode());
        testRuntime.addFrameworkExtensions(getFrameworkExtensions());
        if (bundleStartLevel != null) {
            for (BundleStartLevel level : bundleStartLevel) {
                testRuntime.addBundleStartLevel(level);
            }
        }

        TestFrameworkProvider provider = providerHelper.selectProvider(getProjectType().getClasspath(project),
                getMergedProviderProperties(), providerHint);
        createSurefireProperties(provider);
        for (ArtifactDescriptor artifact : testRuntimeArtifacts.getArtifacts(ArtifactType.TYPE_ECLIPSE_PLUGIN)) {
            // note that this project is added as directory structure rooted at project basedir.
            // project classes and test-classes are added via dev.properties file (see #createDevProperties())
            // all other projects are added as bundle jars.
            ReactorProject otherProject = artifact.getMavenProject();
            if (otherProject != null) {
                if (otherProject.sameProject(project)) {
                    testRuntime.addBundle(artifact.getKey(), project.getBasedir());
                    continue;
                }
                File file = otherProject.getArtifact(artifact.getClassifier());
                if (file != null) {
                    testRuntime.addBundle(artifact.getKey(), file);
                    continue;
                }
            }
            testRuntime.addBundle(artifact);
        }

        Set<Artifact> testFrameworkBundles = providerHelper.filterTestFrameworkBundles(provider, pluginArtifacts);
        for (Artifact artifact : testFrameworkBundles) {
            DevBundleInfo devInfo = workspaceState.getBundleInfo(session, artifact.getGroupId(),
                    artifact.getArtifactId(), artifact.getVersion(), project.getPluginArtifactRepositories());
            if (devInfo != null) {
                testRuntime.addBundle(devInfo.getArtifactKey(), devInfo.getLocation(), true);
                testRuntime.addDevEntries(devInfo.getSymbolicName(), devInfo.getDevEntries());
            } else {
                File bundleLocation = artifact.getFile();
                ArtifactKey bundleArtifactKey = getBundleArtifactKey(bundleLocation);
                testRuntime.addBundle(bundleArtifactKey, bundleLocation, true);
            }
        }

        testRuntime.addDevEntries(getTestBundleSymbolicName(), getBuildOutputDirectories());

        reportsDirectory.mkdirs();
        return installationFactory.createInstallation(testRuntime, work);
    } **/

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

    /**
     * boolean failIfNoTests = Boolean.parseBoolean(testProps.getProperty("failifnotests", "false"));
     boolean redirectTestOutputToFile = Boolean.parseBoolean(testProps.getProperty("redirectTestOutputToFile",
     "false"));
     String testPlugin = testProps.getProperty("testpluginname");
     File testClassesDir = new File(testProps.getProperty("testclassesdirectory"));
     File reportsDir = new File(testProps.getProperty("reportsdirectory"));
     String provider = testProps.getProperty("testprovider");
     String runOrder = testProps.getProperty("runOrder");
     */


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
        props.setProperty("testpluginname", plugin.getBundleID())
        props.setProperty("testclassesdirectory", sourcesetMain.output.classesDir.absolutePath)
        props.setProperty("reportsdirectory", "build/reports")
        props.setProperty("redirectTestOutputToFile", "true") //TODO make configurable
        props.setProperty("failifnotests", uiTestDsl.failIfNoTests.toString())
        props.setProperty("runOrder", "filesystem") //TODO make configurable
        props.setProperty("testprovider", "org.apache.maven.surefire.junit4.JUnit4Provider") //TODO make configurable
        props.setProperty("excludes",'**/Abstract*Test.class,**/Abstract*TestCase.class,**/*$*') //TODO make configurable
        props.setProperty("includes", "**/Test*.class,**/*Test.class,**/*TestCase.class")   //TODO make configurable


        //TODO add properties here
        props.store(new FileOutputStream(surefireProperties), "created by egripse plugin")

    }




    //org.eclipse.tycho.surefire.osgibooter.headlesstest   ->Headlesstest


}
