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

    public void synchronizeResourcesFromBuildProperties(final Project project, final BuildProperties buildproperties) {
        //Add binincludes TODO make nicer
        Copy task = project.tasks.findByName("processResources")
        if (task == null)
            throw new IllegalStateException("processResources not found, javaplugin not applied")

        Copy copyFromBuildProperties = project.tasks.create(type:Copy, name:'copyFromBuildProperties')
        copyFromBuildProperties.destinationDir = task.destinationDir
        copyFromBuildProperties.from(project.projectDir)
        for (String next: buildproperties.binIncludes) {
            log.debug("Add " + next + " to processResources")
            copyFromBuildProperties.include(next)
        }

        copyFromBuildProperties.doLast {
            File manifestFile = project.file("build/resources/main/META-INF/MANIFEST.MF")
            if (manifestFile.exists()) {
              BufferedInputStream bis = new BufferedInputStream(new FileInputStream(manifestFile))
              MetaInf metainf = new MetaInf(manifestFile, bis)
              metainf.setVersion(project.version)
              metainf.save()
              log.info("Set version of ${manifestFile.absolutePath} to " + project.version)
            }

            File featureXmlFile = project.file("build/resources/main/feature.xml")
            if (featureXmlFile.exists()) {
                FeatureXml featureXml = new FeatureXml(featureXmlFile)
                featureXml.setVersion(project.version)
                featureXml.save()
                log.info("Set version of ${featureXmlFile.absolutePath} to " + project.version)
            }

        }

        task.dependsOn(copyFromBuildProperties)
    }
}
