package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class Dependency {

    String bundleID
    String version

    boolean reexported = false
    boolean optional = false

    EclipsePlugin resolvedPlugin

    public Dependency (final String fromString) {
        log.info("Create new dependency " + System.identityHashCode(this) + ": " + fromString)

        List <String> items = fromString.split(";")
        if (items.contains("visibility:=reexport"))
            reexported = true

        if (items.contains("resolution:=optional"))
            optional = true

        bundleID = items.get(0)

        if (bundleID != null)
            bundleID = bundleID.trim()

    }

    public String getIdentifier () {
        return bundleID //TODO add version
    }

    public boolean isResolvable (final EclipsePlugin plugin) {
        if (! bundleID.equals(plugin.bundleID))
            return false

        //TODO handle versions
        if (bundleID.equals("org.junit") && plugin.originPath.name.contains("_3"))
            return false

        return true

    }



    public String toString() {
        return "Dependency " + bundleID + "(reexported=" + reexported + ", optional=" + optional + ")"
    }

    public boolean isResolved () {
        return resolvedPlugin != null
    }
}
