package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 15:14
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class EclipseFeature extends EclipseProjectPart  {

    File featurepath

    FeatureXml featureXml

    EclipseFeature(File path) {
        super (path)
        this.featurepath = path
        log.debug("Create feature for path " + path.absolutePath)

        File featureXmlFile = new File (path, "feature.xml")
        if (featureXmlFile.exists())
          featureXml = new FeatureXml(featureXmlFile)

    }

    @Override
    String getVersion() {
        return featureXml != null ? featureXml.version : null
    }
}
