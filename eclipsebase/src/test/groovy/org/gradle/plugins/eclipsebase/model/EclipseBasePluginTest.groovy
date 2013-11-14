package org.gradle.plugins.eclipsebase.model

import org.gradle.api.internal.project.ProjectInternal
import org.gradle.plugins.eclipsebase.updatesite.CreateCategoriesUpdatesiteTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 12.11.13
 * Time: 14:09
 * To change this template use File | Settings | File Templates.
 */
class EclipseBasePluginTest {

    @Test
    public void featureXmlNotConfigured () {
        ProjectInternal project = ProjectBuilder.builder().build()
        project.apply plugin: 'eclipsebase'

        CreateCategoriesUpdatesiteTask task = project.tasks.updatesiteCategories
        Assert.assertNull (task.findCategoriesXml())
    }

    @Test(expected = IllegalStateException)
    public void featureXmlNotFound () {
        ProjectInternal project = ProjectBuilder.builder().build()
        project.apply plugin: 'eclipsebase'
        project.eclipsebase.updatesite.categoriesXml 'invalidCategoriesXml'

        CreateCategoriesUpdatesiteTask task = project.tasks.updatesiteCategories
        task.findCategoriesXml()
    }
}
