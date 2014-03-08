package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

import java.util.jar.JarFile

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 24.06.13
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class Targetplatform extends DefaultPluginContainer {

    private File localPath

    private Collection <EclipsePlugin> containedPlugins = new ArrayList<EclipsePlugin>()

    private Project project

    public Targetplatform (final Project project, final File localPath) {
        this.project = project
        this.localPath = localPath

        if (! containedPlugins.isEmpty())
            throw new IllegalStateException("We only have to select the contained plugins once")
        containedPlugins = read()
    }

    public FileCollection getUpdatesiteProgramsClasspath () {
        return project.fileTree (pluginsPath) {
            include 'org.eclipse.equinox.launcher_*.jar'
        }
    }

    private File getPluginsPath () {
        return new File (localPath, "plugins")
    }

    public Collection <EclipsePlugin> read () {
        long before = System.currentTimeMillis()
        Collection <EclipsePlugin> readPlugins = new ArrayList<EclipsePlugin>()
        File pluginPath = getPluginsPath()

        for (File next: pluginPath.listFiles()) {
            if (next.name.startsWith("."))
                continue

            if (next.isDirectory() && isPluginPath(next))
                readPlugins.add(new EclipsePlugin(next))
            else if (next.name.endsWith(".jar"))
                readPlugins.add(new EclipsePlugin(new JarFile(next), next))
            else
              log.warn("File ${next.absolutePath} is neither a directory nor a jarfile")
        }

        long delta = System.currentTimeMillis() - before
        log.info("Finished reading targetplatform ${localPath} in ${delta} ms (read ${readPlugins.size()} plugins)")
        return readPlugins
    }

    private boolean isPluginPath (final File path) {
        return new File (path.absoluteFile, 'META-INF').exists()
    }



    public boolean resolvePlugin (String prefix, final Set<EclipsePlugin> resolvedPlugins, final Set<String> resolvedDependencies, final Dependency dependency, final EclipsePlugin plugin) {
        if (resolvedDependencies.contains(dependency.identifier) ) // dependency.optional ||
            return true

        prefix += "  "

        if (dependency == null)
            throw new NullPointerException("dependency must not be null")

        for (EclipsePlugin nextPlugin: containedPlugins) {
            if (nextPlugin.metainf == null) {
                log.info (prefix + "- Plugin " + nextPlugin.originPath.absolutePath + " has no metainf")
                continue
            }

            if (nextPlugin.bundleID == null) {
                log.info (prefix + "- Plugin " + nextPlugin.originPath.absolutePath + " has metainf, but no bundleID")
                continue
            }

            if (dependency.isResolvable(nextPlugin)) {
                log.info (prefix + "- Resolved dependency " + dependency.bundleID + "(" + System.identityHashCode(dependency)
                          + ") to " + nextPlugin.originPath.absolutePath + "(" + System.identityHashCode(dependency) + ")")
                dependency.resolvedPlugin = nextPlugin

                resolvedPlugins.add(nextPlugin)

                for (Dependency nextDep: nextPlugin.metainf.dependencies) {
                    resolvePlugin(prefix, resolvedPlugins, resolvedDependencies, nextDep, nextPlugin)
                }

                resolvedDependencies.add(dependency.identifier)

                return true
            }
        }

        throw new IllegalStateException("Dependency " + dependency.bundleID + " for bundle " + plugin.bundleID + " was not found in targetplatform ")

    }


    public Collection <EclipsePlugin> getPlugins () {
        return containedPlugins
    }

    @Override
    String getIdentifier() {
        return localPath.absolutePath
    }
}
