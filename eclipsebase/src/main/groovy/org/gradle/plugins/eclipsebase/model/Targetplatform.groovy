package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j
import org.apache.tools.ant.taskdefs.condition.Os
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

    private Collection <EclipsePlugin> containedPlugins = new ArrayList<EclipsePlugin>()

    Project project

    public Targetplatform (final Project project) {
        this.project = project
        if (! containedPlugins.isEmpty())
            throw new IllegalStateException("We only have to select the contained plugins once")
        containedPlugins = read()
    }

    public File getPath () {
        return new File (System.getProperty("user.home"), ".goomph")
    }

    private File idePath (final Project project) {
        File bundlesInfoFile = project.file('build/oomph-ide.app/Contents/Eclipse')
        if (!bundlesInfoFile.exists())
            bundlesInfoFile = project.file('build/oomph-ide/')

        return bundlesInfoFile
    }

    List <EclipsePlugin> read () {
        File idePath = idePath(project)
        File bundlesInfoFile = new File (idePath, 'configuration/org.eclipse.equinox.source/source.info')
        if (! bundlesInfoFile.exists())
            throw new IllegalStateException("Bundles Info file does not exist in project " + project.name + "(" + bundlesInfoFile.absolutePath + ")")
        BundlesInfo bundlesInfo = new BundlesInfo(bundlesInfoFile)
        Collection<EclipsePlugin> eclipsePluginCollection = new ArrayList<EclipsePlugin>()
        for (BundlesInfoEntry nextEntry: bundlesInfo.entries) {
            log.info("Found bundle info " + nextEntry.bundleID + ", " + nextEntry.version + ", " + nextEntry.bundleID)
            File jarFile = new File (idePath.absolutePath + File.separator + nextEntry.ref).canonicalFile
            if (! jarFile.exists())
                throw new IllegalStateException("Referenced jarfile " + jarFile.getAbsolutePath() + " does not exist (ide path " + idePath.absolutePath + ")")

            addPlugin(eclipsePluginCollection, jarFile)
        }

        return eclipsePluginCollection
    }

    protected boolean isPluginPath (final File path) {
        return new File (path.absoluteFile, 'META-INF').exists()
    }

    public void addPlugin (final List<EclipsePlugin> eclipsePlugins, final File next) {
        if (next.isDirectory() && isPluginPath(next))
            eclipsePlugins.add(new EclipsePlugin(next))
        else if (next.name.endsWith(".jar"))
            eclipsePlugins.add(new EclipsePlugin(new JarFile(next), next))
        else
            log.warn("File ${next.absolutePath} is neither a directory nor a jarfile")
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
        return "oomph"
    }
}
