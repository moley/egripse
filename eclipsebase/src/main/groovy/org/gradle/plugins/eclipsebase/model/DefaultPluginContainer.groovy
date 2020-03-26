package org.gradle.plugins.eclipsebase.model
/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 28.06.13
 * Time: 12:24
 * To change this template use File | Settings | File Templates.
 */
abstract class DefaultPluginContainer implements PluginContainer {

    /**
     * finds first fragment plugin
     * @param host plugin
     * @return
     */
    public EclipsePlugin findFragmentPlugin (final String host) {
        for (EclipsePlugin nextPlugin: getPlugins()) {
            if (nextPlugin.fragmentHost.equals(host))
                return nextPlugin
        }

    }

    public EclipsePlugin findBundleByID (final String id) {
        for (EclipsePlugin nextPlugin: getPlugins()) {
            if (nextPlugin.bundleID.equals(id))
                return nextPlugin
        }
        return null
    }

    public EclipsePlugin findPluginByBundleID(final String bundleID) {
        for (EclipsePlugin plugin : getPlugins()) {
            if (plugin.metainf.bundleID.equals(bundleID)) {
                return plugin
            }
        }
        return null
    }

    public abstract String getIdentifier ()

    public List<File> getAllPluginPaths() {
        List<File> allSourceFolders = new ArrayList<File>()

        for (EclipsePlugin plugin : getPlugins()) {
            allSourceFolders.add(plugin.originPath)
        }

        return allSourceFolders

    }



    public EclipsePlugin findPluginByPath(final File path) {
        for (EclipsePlugin plugin : getPlugins()) {
            if (plugin.originPath.equals(path))
                return plugin
        }
        return
    }

    public String toString () {
        String logstring = "PluginContainer " + getClass().getName() + "-" + getIdentifier() + ":\n"
        for (EclipsePlugin nextPlugin: getPlugins()) {
            logstring += "- " + nextPlugin.bundleID + "\n"
        }

        return logstring
    }





}
