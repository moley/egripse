package org.gradle.plugins.eclipsebase.config

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.plugins.eclipsebase.model.BuildProperties
import org.gradle.plugins.eclipsebase.model.FeatureXml
import org.gradle.plugins.eclipsebase.model.MetaInf

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 03.07.13
 * Time: 08:14
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class BuildPropertiesConfigurator {

    public void synchronizeResourcesFromBuildProperties(final Project project, final BuildProperties buildproperties,
                                                        final boolean forceManifest, final boolean forceFeatureXml) {
        //Add binincludes TODO make nicer
        Copy processResourcesTask = project.tasks.findByName("processResources")
        if (processResourcesTask == null)
            throw new IllegalStateException("processResources not found, javaplugin not applied")

        File mergedResourcesFolder = project.file("build/mergedResources")

        project.copy {
            into(mergedResourcesFolder)
            from(project.projectDir) {
                log.info("Included " + buildproperties.binIncludes)
                include(buildproperties.binIncludes)
            }
        }

        File manifestFile = new File (mergedResourcesFolder, "META-INF/MANIFEST.MF")
        if (manifestFile.exists()) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(manifestFile))
            MetaInf metainf = new MetaInf(manifestFile, bis)
            metainf.setVersion(project.version)
            metainf.save()
            project.jar.manifest.from {manifestFile}
            log.info("Set version of manifest ${manifestFile.absolutePath} to " + project.version)
        }
        else {
            if (forceManifest)
                throw new IllegalStateException("Project has no MANIFEST.MF file distributed")
            else
                log.warn("Manifest file <" + manifestFile.absolutePath + "> not available")
        }

        File featureXmlFile = new File (mergedResourcesFolder, "feature.xml")
        if (featureXmlFile.exists()) {
            FeatureXml featureXml = new FeatureXml(featureXmlFile)
            featureXml.setVersion(project.version)
            featureXml.save()
            log.info("Set version of featureXml ${featureXmlFile.absolutePath} to " + project.version)
        }
        else {
            if (forceFeatureXml)
                throw new IllegalStateException("Project has no feature.xml file distributed")
            else
              log.warn("Feature.xml file <" + featureXmlFile.absolutePath + "> not available")
        }



    }
}
