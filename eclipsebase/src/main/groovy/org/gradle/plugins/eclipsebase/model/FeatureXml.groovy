package org.gradle.plugins.eclipsebase.model

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 03.07.13
 * Time: 08:26
 * To change this template use File | Settings | File Templates.
 */
class FeatureXml {

    String featureID

    String version

    def parsedProjectXml

    final File file

    public FeatureXml (final File featureXmlFile) {
        this.file = featureXmlFile
        parsedProjectXml = (new XmlParser()).parse(featureXmlFile)
        version = parsedProjectXml.@version
        featureID = parsedProjectXml.@id
    }

    public void setVersion (final String version) {
        parsedProjectXml.@version = version
    }

    public void save () {
        def writer = new FileWriter(file)
        new XmlNodePrinter(new PrintWriter(writer)).print(parsedProjectXml)
    }
}
