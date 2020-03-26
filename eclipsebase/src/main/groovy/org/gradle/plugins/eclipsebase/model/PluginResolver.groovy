package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j
import org.gradle.api.Project

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 28.06.13
 * Time: 12:29
 * To change this template use File | Settings | File Templates.
 */
class PluginResolver {


    public boolean resolvePlugin (Eclipse eclipse,
                                  String prefix,
                                  final Set<EclipsePlugin> resolvedPlugins,
                                  final Set<String> resolvedDependencies,
                                  final Dependency dependency,
                                  final EclipsePlugin plugin) {

        Project project = eclipse.project
        
        if (resolvedDependencies.contains(dependency.identifier))
            return false

        prefix += "  "

        if (dependency == null)
            throw new NullPointerException("dependency must not be null")

        String statelogger = ""

        for (PluginContainer nextContainer : eclipse.pluginContainers) {
            for (EclipsePlugin nextPlugin : nextContainer.getPlugins()) {
                if (nextPlugin.metainf == null) {
                    project.logger.info(prefix + "- Plugin " + nextPlugin.originPath.absolutePath + " has no metainf")
                    continue
                }

                if (nextPlugin.bundleID == null) {
                    project.logger.info(prefix + "- Plugin " + nextPlugin.originPath.absolutePath + " has metainf, but no bundleID")
                    continue
                }

                if (dependency.isResolvable(nextPlugin)) {
                    project.logger.info(prefix + "- Resolved dependency <" + dependency.bundleID + "> (" + System.identityHashCode(dependency)
                            + ") to <" + nextPlugin.originPath.absolutePath + "> (" + System.identityHashCode(dependency) + ")")
                    dependency.resolvedPlugin = nextPlugin

                    resolvedPlugins.add(nextPlugin)

                    for (Dependency nextDep : nextPlugin.metainf.dependencies) {
                        resolvePlugin(eclipse, prefix, resolvedPlugins, resolvedDependencies, nextDep, nextPlugin)
                    }

                    resolvedDependencies.add(dependency.identifier)

                    return true
                }
            }
            statelogger += nextContainer.toString()
        }

        if (! dependency.optional)
          throw new IllegalStateException(statelogger + "\nDependency <" + dependency.bundleID + "> for bundle <" + plugin.bundleID + "> was not found in any plugincontainer")

    }
}
