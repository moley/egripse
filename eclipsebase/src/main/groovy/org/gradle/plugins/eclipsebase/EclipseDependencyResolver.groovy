package org.gradle.plugins.eclipsebase


import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.plugins.eclipsebase.model.Dependency
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.EclipsePlugin
import org.gradle.plugins.eclipsebase.model.PluginResolver

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 11.10.13
 * Time: 21:58
 * To change this template use File | Settings | File Templates.
 */
class EclipseDependencyResolver {

    /**
     * resolves dependencies of all subprojects
     * @param rootProject rootproject
     */
    public void resolve (Project rootProject) {
        rootProject.logger.info("resolve dependencies for rootproject " + rootProject.name + " with " + rootProject.subprojects.size()+ " subprojects")

        Eclipse eclipseModel = rootProject.eclipsemodel

        for (Project next: rootProject.subprojects) {
          rootProject.logger.info("Resolve dependencies of project " + next.project)

          next.plugins.apply(JavaPlugin) //We need for compile configuration

          //we only resolve projects that are plugins
          EclipsePlugin currentEclipseplugin = eclipseModel.workspace.findPluginByPath(next.projectDir)
          if (currentEclipseplugin != null) {
              rootProject.logger.info ("Resolve dependencies of plugin project " + currentEclipseplugin.bundleID)
            addBundleClasspathEntries(next, currentEclipseplugin)
            resolveDependencies(next, eclipseModel, currentEclipseplugin)
          }
        }
    }

    private void addBundleClasspathEntries (final Project project, final EclipsePlugin currentEclipseplugin) {
        rootProject.logger.info ("Adding bundle-classpath-entries in project ${currentEclipseplugin.bundleID}...")
        for (String nextBundleClasspathEntry : currentEclipseplugin.metainf.bundleClasspath) {
            rootProject.logger.info("Adding dependency to bundleclass " + nextBundleClasspathEntry)
            project.dependencies.add("compile", project.files(nextBundleClasspathEntry))
        }
        rootProject.logger.info ("Finished bundle-classpath-entries")
    }

    private void resolveDependencies (final Project project, final Eclipse eclipsemodel, final EclipsePlugin currentEclipseplugin) {
        rootProject.logger.info("resolve dependencies called in project " + project.name)
        //Resolve external bundles
        String projectName = project.name

        PluginResolver pluginresolver = new PluginResolver()

        rootProject.logger.info ("Resolving dependend bundles in project ${project}...")
        Set <EclipsePlugin> resolvedExternalPlugins = new HashSet<EclipsePlugin>()
        Set<Dependency> resolvedDependencies = new HashSet<Dependency>()
        for (Dependency nextDependency: currentEclipseplugin.metainf.dependencies) {
            rootProject.logger.info("Resolving dependency " + nextDependency.bundleID)
            pluginresolver.resolvePlugin(eclipsemodel, "in project ${projectName} ", resolvedExternalPlugins, resolvedDependencies, nextDependency, currentEclipseplugin)
        }
        rootProject.logger.info ("Finished resolving bundles (" + resolvedExternalPlugins.size() + " deps) in project ${project}")

        //Add resolved jars
        rootProject.logger.info ("Add all resolved bundles in project ${project}...")

        Collection <File> externalPluginsAsFile = new ArrayList<File>()
        for (EclipsePlugin nextPlugin: resolvedExternalPlugins) {

            if (nextPlugin.fromWorkspace) {
                String pathToBundle = ":" + nextPlugin.bundleID + ""

                rootProject.logger.info("Adding dependency in project ${projectName} to project " + pathToBundle)

                project.dependencies.add("compile", project.dependencies.project(path: pathToBundle))
                Project depProject = project.rootProject.project(nextPlugin.bundleID)
                rootProject.logger.info("Lookup project " + depProject.name)
                project.dependencies.add("compile", project.files(depProject.sourceSets.test.output)) //test sources of dependend projects

            } else {

                for (File next : nextPlugin.bundleClasspath) {
                    rootProject.logger.info("Adding bundleclass in project ${projectName} from plugin " + nextPlugin.bundleID + ": " + next.absolutePath)
                    externalPluginsAsFile.add(next.absolutePath)
                }

                rootProject.logger.info("Adding dependency in project ${projectName} to plugin " + nextPlugin.bundleID + "(" + nextPlugin.originPath.name + ")")
                externalPluginsAsFile.add(nextPlugin.originPath)

                //TODO generic fragment host handling
                if (nextPlugin.bundleID.equals("org.eclipse.swt")) {

                    EclipsePlugin fragmentHost = eclipsemodel.targetplatformModel.findFragmentPlugin(nextPlugin.bundleID)
                    if (fragmentHost == null)
                        throw new IllegalStateException("No Fragment plugin found with host plugin ${nextPlugin.bundleID}")

                    rootProject.logger.info("Adding platform swt dependency " + fragmentHost.originPath.name)
                    externalPluginsAsFile.add(project.files(fragmentHost.originPath))
                }
            }
        }

        project.dependencies.add("compile", project.files(externalPluginsAsFile))

        rootProject.logger.info ("Finished adding resolved bundles in project ${project}")

    }
}
