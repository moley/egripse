package org.gradle.plugins.eclipseplugin.model

import org.gradle.api.Project
import org.gradle.plugins.eclipsebase.model.Targetplatform
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 24.06.13
 * Time: 22:16
 * To change this template use File | Settings | File Templates.
 */
class TargetplatformTest {

    @Test
    public void read () {

        Project project = ProjectBuilder.builder().build()

        Targetplatform targetplatform = new Targetplatform(project, new File ("src/test/resources/targetplatform/eclipse"))
        Assert.assertEquals (targetplatform.plugins.toString(), 1, targetplatform.plugins.size())
        Assert.assertEquals ("org.springsource.ide.eclipse.commons.core", targetplatform.getPlugins().iterator().next().metainf.bundleID)

    }
}
