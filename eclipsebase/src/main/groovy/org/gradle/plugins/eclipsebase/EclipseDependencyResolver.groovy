package org.gradle.plugins.eclipsebase

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.artifacts.configurations.DefaultConfiguration
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
@Slf4j
class EclipseDependencyResolver {

    /**
     * resolves dependencies of all subprojects
     * @param rootProject rootproject
     */
    public void resolve (Project rootProject) {

        Eclipse eclipseModel = rootProject.eclipsemodel

        for (Project next: rootProject.subprojects) {
          log.info("Resolve dependencies of " + next.project)

          next.plugins.apply(JavaPlugin) //We need for compile configuration

          //we only resolve projects that are plugins
          EclipsePlugin currentEclipseplugin = eclipseModel.workspace.findPluginByPath(next.projectDir)
          if (currentEclipseplugin != null) {
            addBundleClasspathEntries(next, currentEclipseplugin)
            resolveDependencies(next, eclipseModel, currentEclipseplugin)
          }
        }
    }

    private void addBundleClasspathEntries (final Project project, final EclipsePlugin currentEclipseplugin) {
        log.info ("Adding bundle-classpath-entries in project ${currentEclipseplugin.bundleID}...")
        for (String nextBundleClasspathEntry : currentEclipseplugin.metainf.bundleClasspath) {
            log.info("Adding dependency to bundleclass " + nextBundleClasspathEntry)
            project.dependencies.add("compile", project.files(nextBundleClasspathEntry))
        }
        log.info ("Finished bundle-classpath-entries")
    }

    private void resolveDependencies (final Project project, final Eclipse eclipsemodel, final EclipsePlugin currentEclipseplugin) {
        //Resolve external bundles
        String projectName = project.name

        PluginResolver pluginresolver = new PluginResolver()

        log.info ("Resolving dependend bundles in project ${project}...")
        Set <EclipsePlugin> resolvedExternalPlugins = new HashSet<EclipsePlugin>()
        Set<Dependency> resolvedDependencies = new HashSet<Dependency>()
        for (Dependency nextDependency: currentEclipseplugin.metainf.dependencies) {
            log.info("Resolving dependency " + nextDependency.bundleID)
            pluginresolver.resolvePlugin(eclipsemodel, "in project ${projectName} ", resolvedExternalPlugins, resolvedDependencies, nextDependency, currentEclipseplugin)
        }
        log.info ("Finished resolving bundles (" + resolvedExternalPlugins.size() + " deps) in project ${project}")

        //Add resolved jars
        log.info ("Add all resolved bundles in project ${project}...")

        Collection <File> externalPluginsAsFile = new ArrayList<File>()
        for (EclipsePlugin nextPlugin: resolvedExternalPlugins) {

            if (nextPlugin.fromWorkspace) {
                String pathToBundle = ":" + nextPlugin.bundleID + ""

                log.info("Adding dependency in project ${projectName} to project " + pathToBundle)

                project.dependencies.add("compile", project.dependencies.project(path: pathToBundle))
                Project depProject = project.rootProject.project(nextPlugin.bundleID)
                log.info("Lookup project " + depProject.name)
                project.dependencies.add("compile", project.files(depProject.sourceSets.test.output)) //test sources of dependend projects

            } else {

                for (File next : nextPlugin.bundleClasspath) {
                    log.info("Adding bundleclass in project ${projectName} from plugin " + nextPlugin.bundleID + ": " + next.absolutePath)
                    externalPluginsAsFile.add(next.absolutePath)
                }

                log.info("Adding dependency in project ${projectName} to plugin " + nextPlugin.bundleID + "(" + nextPlugin.originPath.name + ")")
                externalPluginsAsFile.add(nextPlugin.originPath)

                //TODO fragment host handling
                if (nextPlugin.bundleID.equals("org.eclipse.swt")) {
                    EclipsePlugin platformSwt = eclipsemodel.targetplatformModel.findBundleByID("org.eclipse.swt.gtk.linux.x86")
                    log.info("Adding platform swt dependency " + platformSwt.originPath.name)
                    externalPluginsAsFile.add(project.files(platformSwt.originPath))
                }
            }
        }

        project.dependencies.add("compile", project.files(externalPluginsAsFile))

        FileCollection config = project.sourceSets.main.compileClasspath
        if (config.isEmpty())
            log.warn("Compileclasspath of project " + project.name + " is empty")
        /**for (Object next: config.getFiles()) {
            log.info ("Classpathentry in " + project.name + ": " + next)
        }  **/

        log.info ("Finished adding resolved bundles in project ${project}")

    }
}
