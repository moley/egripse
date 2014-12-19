package org.gradle.plugins.eclipseplugin.model

import org.gradle.plugins.eclipsebase.model.BuildProperties
import org.junit.Ignore
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 24.05.13
 * Time: 00:00
 * To change this template use File | Settings | File Templates.
 */
class BuildPropertiesTest {

    @Test@Ignore
    public void src () {
        //TODO Use src from build.properties to configure sourcesets
    }

    @Test
    public void buildincludes () {
        URL url = getClass().classLoader.getResource("build.properties")
        File file = new File (url.path).absoluteFile

        BuildProperties properties = new BuildProperties(file)
        for (String next: properties.binIncludes) {
            println (next)
        }


    }

    @Test
    public void buildincludesNotReadble () {
        URL url = getClass().classLoader.getResource("build.properties.notreadable")
        File file = new File (url.path).absoluteFile

        BuildProperties properties = new BuildProperties(file)
        for (String next: properties.binIncludes) {
            println (next)
        }


    }

}
