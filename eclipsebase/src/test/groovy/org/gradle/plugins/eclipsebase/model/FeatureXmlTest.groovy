package org.gradle.plugins.eclipsebase.model

import org.junit.Assert
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 03.07.13
 * Time: 08:28
 * To change this template use File | Settings | File Templates.
 */
class FeatureXmlTest {


    @Test
    public void readFile () {
        URL url = getClass().classLoader.getResource("feature.xml")
        File file = new File (url.path).absoluteFile
        FeatureXml xml = new FeatureXml(file)
        Assert.assertEquals ("vsa.marvin.modeling.feature", xml.featureID)
        Assert.assertEquals ("1.0.0.qualifier", xml.version)

    }
}
