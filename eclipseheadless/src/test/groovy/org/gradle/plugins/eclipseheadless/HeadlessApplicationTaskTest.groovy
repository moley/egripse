package org.gradle.plugins.eclipseheadless

import com.google.common.io.Files
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test

/**
 * Created by OleyMa on 19.09.14.
 */
class HeadlessApplicationTaskTest {

    @Test
    public void findEquinoxLauncherOne () {
        File tmpDir = Files.createTempDir()

        File correct = new File (tmpDir, 'org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar')
        Assert.assertTrue (correct.createNewFile())

        File incorrect1 = new File (tmpDir, 'org.eclipse.equinox.launcher.cocoa.macosx.x86_64_1.1.200.v20130729-1429')
        Assert.assertTrue (incorrect1.mkdirs())

        File found = task.findEquinoxLauncher(tmpDir)
        Assert.assertEquals ("Wrong jar found", correct, found)

    }

    private HeadlessApplicationTask getTask () {
        Project project = ProjectBuilder.builder().build()
        return project.task('buildLuna', type: org.gradle.plugins.eclipseheadless.HeadlessApplicationTask) {}
    }

    @Test(expected=IllegalStateException)
    public void findEquinnoxLauncherMultiple () {
        File tmpDir = Files.createTempDir()

        File correct = new File (tmpDir, 'org.eclipse.equinox.launcher_1.3.0.v20140415-2008.jar')
        Assert.assertTrue (correct.createNewFile())

        File correct2 = new File (tmpDir, 'org.eclipse.equinox.launcher_1.4.0.v20140415-2008.jar')
        Assert.assertTrue (correct2.createNewFile())

        task.findEquinoxLauncher(tmpDir)

    }

    @Test(expected=IllegalStateException)
    public void findEquinoxLauncherNone () {
        File tmpDir = Files.createTempDir()

        File correct = new File (tmpDir, 'org.eclipse.equinox.lasuncher_1.3.0.v20140415-2008.jar')
        Assert.assertTrue (correct.createNewFile())

        task.findEquinoxLauncher(tmpDir)

    }
}
