package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project

import java.util.jar.JarFile

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 24.06.13
 * Time: 21:45
 * To change this template use File | Settings | File Templates.
 */
class Targetplatform extends DefaultPluginContainer {

    private Collection<EclipsePlugin> containedPlugins = new ArrayList<EclipsePlugin>()

    Project project

    public Targetplatform(final Project project) {
        this.project = project
        if (!containedPlugins.isEmpty())
            throw new IllegalStateException("We only have to select the contained plugins once")
        containedPlugins = read()
    }

    public File getPath() {
        return new File(System.getProperty("user.home"), ".goomph")
    }

    public File idePath(final Project project) {
        File idePath = project.file('build/oomph-ide.app/Contents/Eclipse')
        if (!idePath.exists())
            idePath = project.file('build/oomph-ide/')

        if (!idePath.exists())
            throw new IllegalStateException("ide path not found in project " + project.projectDir.absolutePath)

        return idePath
    }

    public File executableEclipse(final Project project) {

        File currentPath = Os.isFamily(Os.FAMILY_MAC) ? project.file('build/oomph-ide.app') : project.file('build/oomph-ide')

        String executableToken;
        if (Os.isFamily(Os.FAMILY_MAC))
            executableToken = "Contents/MacOS/eclipse"
        else if (Os.isFamily(Os.FAMILY_WINDOWS))
            executableToken = "eclipsec.exe"
        else
            executableToken = "eclipse";

        File executableFile = new File(currentPath, executableToken)
        if (executableFile.exists())
            return executableFile
        else
            throw new IllegalStateException("Executable eclipse not found in project " + project.projectDir.absolutePath + " (expected path " + currentPath.absolutePath + ", expected executable token " + executableToken + ")")
    }

    List<EclipsePlugin> read() {
        File idePath = idePath(project)
        File bundlesInfoFile = new File(idePath, 'configuration/org.eclipse.equinox.simpleconfigurator/bundles.info')
        List<BundlesInfoEntry> bundlesInfoEntries = new ArrayList<BundlesInfoEntry>()
        if (bundlesInfoFile.exists()) {
            BundlesInfo bundlesInfo = new BundlesInfo(bundlesInfoFile)
            bundlesInfoEntries = bundlesInfo.entries
        } else throw new IllegalStateException("File " + bundlesInfoFile.absolutePath + " does not exist in path $idePath.absolutePath, please check if your platform is supported by eclipse")

        if (bundlesInfoEntries.isEmpty())
            throw new IllegalStateException("No bundle infos are available in path " + idePath.absolutePath)

        Collection<EclipsePlugin> eclipsePluginCollection = new ArrayList<EclipsePlugin>()
        for (BundlesInfoEntry nextEntry : bundlesInfoEntries) {
            project.logger.info("Found bundle info " + nextEntry.bundleID + ", " + nextEntry.version + ", " + nextEntry.bundleID)
            File jarFile = new File(nextEntry.ref).isAbsolute() ? new File(nextEntry.ref) : new File(idePath.absolutePath + File.separator + nextEntry.ref).canonicalFile
            if (!jarFile.exists())
                throw new IllegalStateException("Referenced jarfile " + jarFile.getAbsolutePath() + " does not exist (ide path " + idePath.absolutePath + ")")

            addPlugin(eclipsePluginCollection, jarFile)
        }

        return eclipsePluginCollection
    }

    protected boolean isPluginPath(final File path) {
        return new File(path.absoluteFile, 'META-INF').exists()
    }

    public void addPlugin(final List<EclipsePlugin> eclipsePlugins, final File next) {
        if (next.isDirectory() && isPluginPath(next))
            eclipsePlugins.add(new EclipsePlugin(next))
        else if (next.name.endsWith(".jar"))
            eclipsePlugins.add(new EclipsePlugin(new JarFile(next), next))
        else
            project.logger.warn("File ${next.absolutePath} is neither a directory nor a jarfile")
    }


    public boolean resolvePlugin(String prefix, final Set<EclipsePlugin> resolvedPlugins, final Set<String> resolvedDependencies, final Dependency dependency, final EclipsePlugin plugin) {
        if (resolvedDependencies.contains(dependency.identifier)) // dependency.optional ||
            return true

        prefix += "  "

        if (dependency == null)
            throw new NullPointerException("dependency must not be null")

        for (EclipsePlugin nextPlugin : containedPlugins) {
            if (nextPlugin.metainf == null) {
                project.logger.info(prefix + "- Plugin " + nextPlugin.originPath.absolutePath + " has no metainf")
                continue
            }

            if (nextPlugin.bundleID == null) {
                project.logger.info(prefix + "- Plugin " + nextPlugin.originPath.absolutePath + " has metainf, but no bundleID")
                continue
            }

            if (dependency.isResolvable(nextPlugin)) {
                project.logger.info(prefix + "- Resolved dependency " + dependency.bundleID + "(" + System.identityHashCode(dependency)
                        + ") to " + nextPlugin.originPath.absolutePath + "(" + System.identityHashCode(dependency) + ")")
                dependency.resolvedPlugin = nextPlugin

                resolvedPlugins.add(nextPlugin)

                for (Dependency nextDep : nextPlugin.metainf.dependencies) {
                    resolvePlugin(prefix, resolvedPlugins, resolvedDependencies, nextDep, nextPlugin)
                }

                resolvedDependencies.add(dependency.identifier)

                return true
            }
        }

        throw new IllegalStateException("Dependency " + dependency.bundleID + " for bundle " + plugin.bundleID + " was not found in targetplatform ")

    }


    public Collection<EclipsePlugin> getPlugins() {
        return containedPlugins
    }

    @Override
    String getIdentifier() {
        return "oomph"
    }
}
