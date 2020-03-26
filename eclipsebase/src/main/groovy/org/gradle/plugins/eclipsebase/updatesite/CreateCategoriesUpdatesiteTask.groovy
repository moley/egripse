package org.gradle.plugins.eclipsebase.updatesite
import groovy.util.logging.Slf4j
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipsebase.dsl.EclipseBaseDsl
import org.gradle.plugins.eclipsebase.dsl.UpdatesiteDsl
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.Targetplatform

/**
 * Create categories for the updatesite,
 * see details: https://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fp2_publisher.html
 */
class CreateCategoriesUpdatesiteTask extends Exec {

    /**
     * gets categoriesxml and throws error if not configured
     * or not found
     * @return categories xml file that is ensured to exist
     */
    public File findCategoriesXml () {
        EclipseBaseDsl basedsl = project.eclipsebase
        UpdatesiteDsl updatesite = basedsl.updatesite
        if (updatesite.categoriesXml == null) {
            project.logger.warn("No category.xml file configured to be used. Skip creating categories")
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

        Targetplatform externalEclipse = eclipse.targetplatformModel

        File updatesitePath = project.file("build/updatesite")
        File categoryDefinition = findCategoriesXml()
        if (categoryDefinition == null) {
            println "No category definition found in project $project.projectDir.absolutePath"
            return
        }

        println "Category definition          : " + categoryDefinition.absolutePath
        println "Updatesite                   : " + updatesitePath.absolutePath

        project.logger.info("Classpath " + getClass().getName())

        workingDir 'build/newUpdatesiteContent'

        executable(externalEclipse.executableEclipse(project).absolutePath)

        args '-Xmx900m'

        args '-application', 'org.eclipse.equinox.p2.publisher.CategoryPublisher'
        args '-metadataRepository', 'file:' + updatesitePath.absolutePath
        args '-categoryDefinition', 'file:' + categoryDefinition.absolutePath
        //args '-compress'
        //args '-console'
        //args '-consoleLog'
        args '-nosplash'

        println String.join(" ", commandLine)

        super.exec()
    }
}
