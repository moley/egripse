package org.gradle.plugins.eclipsebase.model

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 03.07.13
 * Time: 08:10
 * To change this template use File | Settings | File Templates.
 */
class EclipseProjectPart {

    BuildProperties buildProperties

    public EclipseProjectPart ( final File path) {
        if (path != null) {
          File buildPropertiesFile = new File (path, "build.properties")
          buildProperties = new BuildProperties(buildPropertiesFile)
        }
    }

}
