package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j
import org.gradle.api.Project

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 28.06.13
 * Time: 12:23
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class Workspace extends DefaultPluginContainer{

    private Collection<EclipsePlugin> eclipsePlugins

    private Collection<EclipseFeature> eclipseFeatures

    private File workspacePath


    public Workspace (final Project project,
                      final String pluginsPath,
                      final String featuresPath) {

        this.workspacePath = project.projectDir

        log.info("Read components in " + project.projectDir)

        File pluginDir = project.file(pluginsPath)
        log.info("Configured plugindir " + pluginDir)

        for (File next : pluginDir.listFiles()) {
            File nextMetainf = new File(next, "META-INF")
            File metainf = new File(nextMetainf, "MANIFEST.MF")
            if (metainf.exists()) {
                log.info("Adding ${next.absolutePath} as plugin")
                addEclipsePlugin(next)
            }
        }

        File featureDir = project.file(featuresPath)
        log.info("Configured featuredir " + featureDir)

        for (File next : featureDir.listFiles()) {
            File nextFeatureXml = new File(next, "feature.xml")
            if (nextFeatureXml.exists()) {
                log.info("Adding ${next.absolutePath} as feature")
                addEclipseFeature(next)
            }
        }

    }

    public String getProjectPartVersion (final File path ) {
        EclipsePlugin plugin = findPluginByPath(path)
        if (plugin != null)
            return plugin.metainf.version
        else {
            EclipseFeature feature = findFeatureByPath(path)
            return feature.featureXml.version
        }
    }

    public EclipseProjectPart findProjectPart (final File path) {
        EclipsePlugin plugin = findPluginByPath(path)
        if (plugin != null)
            return plugin
        else
            return findFeatureByPath(path)
    }

    public void addEclipsePlugin(File path) {
        if (eclipsePlugins == null)
            eclipsePlugins = new ArrayList<EclipsePlugin>()
        EclipsePlugin plugin = new EclipsePlugin(path)
        plugin.fromWorkspace = true
        eclipsePlugins.add(plugin)
    }


    public EclipseFeature findFeatureByPath(final File path) {
        for (EclipseFeature plugin : eclipseFeatures) {
            if (plugin.featurepath.equals(path))
                return plugin
        }
        return null
    }

    public Collection <EclipsePlugin> getPlugins () {
        return eclipsePlugins
    }

    public void addEclipseFeature(File path) {
        if (eclipseFeatures == null)
            eclipseFeatures = new ArrayList<EclipseFeature>()
        log.debug("Found eclipse feature in path " + path.absolutePath)
        eclipseFeatures.add(new EclipseFeature(path))

    }

    public Collection<EclipseFeature> getEclipseFeatures() {
        return eclipseFeatures
    }

    @Override
    String getIdentifier() {
        return workspacePath.absolutePath
    }

    public Collection<Dependency> getAllDirectDependencies (final boolean includeWorkspacePlugins) {
        log.info("getAllDirectDependencies $includeWorkspacePlugins")
        Set <Dependency> dependencies = new HashSet<Dependency>()
        for (EclipsePlugin next: getPlugins()) {
            for (Dependency nextDep: next.metainf.dependencies) {

                boolean isWorkspacePlugin = (findPluginByBundleID(nextDep.bundleID) != null)
                log.info("check $nextDep.bundleID as workspace plugin: $isWorkspacePlugin")
                if (includeWorkspacePlugins || ! isWorkspacePlugin) {
                    dependencies.add(nextDep)
                    log.info("Add $nextDep.bundleID as direct dependency")
                }

            }

        }

        for (Dependency next: dependencies) {
            log.info("Direct dependency " + next.toString() + "found")
        }

        return dependencies

    }
}
