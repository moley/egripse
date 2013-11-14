package org.gradle.plugins.eclipsebase.updatesite

import groovy.util.logging.Slf4j
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.dsl.EclipseBaseDsl
import org.gradle.plugins.eclipsebase.dsl.UpdatesiteDsl
import org.gradle.plugins.eclipsebase.model.Eclipse

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 04.07.13
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class CreateCategoriesUpdatesiteTask extends JavaExec {

    /**
     * gets categoriesxml and throws error if not configured
     * or not found
     * @return categories xml file that is ensured to exist
     */
    public File findCategoriesXml () {
        EclipseBaseDsl basedsl = project.eclipsebase
        UpdatesiteDsl updatesite = basedsl.updatesite
        if (updatesite.categoriesXml == null) {
            log.warn("No category.xml file configured to be used. Skip creating categories")
            return null
        }

        File categoryDefinition = project.file (updatesite.categoriesXml)
        if (!categoryDefinition.exists())
            throw new IllegalStateException("Configured categoriesXml " + updatesite.categoriesXml + " not found (Looking for " + categoryDefinition.absolutePath + ")")

        return categoryDefinition

    }

    @TaskAction
    public void exec () {

        Eclipse eclipse = project.eclipsemodel

        File updatesitePath = project.file("build/updatesite")
        File categoryDefinition = findCategoriesXml()
        if (categoryDefinition == null)
            return

        log.info("Classpath " + getClass().getName())
        for (File next: eclipse.targetplatformModel.updatesiteProgramsClasspath) {
            log.info("- Entry " + next.absolutePath)
        }

        workingDir 'build/newUpdatesiteContent'
        main 'org.eclipse.core.launcher.Main'
        classpath eclipse.targetplatformModel.updatesiteProgramsClasspath
        jvmArgs '-Xms40m'
        jvmArgs '-Xmx900m'
        jvmArgs '-XX:MaxPermSize=512m'

        args '-application', 'org.eclipse.equinox.p2.publisher.CategoryPublisher'
        args '-metadataRepository', 'file:' + updatesitePath.absolutePath
        args '-categoryDefinition', 'file:' + categoryDefinition.absolutePath
        args '-categoryQualifier'
        args '-compress'
        args '-console'
        args '-consoleLog'
        args (args)

        super.exec()
    }
}
