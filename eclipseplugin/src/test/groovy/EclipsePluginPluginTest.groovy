import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.DefaultSourceSet
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 07.11.13
 * Time: 09:33
 * To change this template use File | Settings | File Templates.
 */
class EclipsePluginPluginTest {

    @Test
    public void sourceProject () {

        ProjectInternal rootproject = ProjectBuilder.builder().build()
        rootproject.apply plugin: 'eclipsebase'

        ProjectInternal project = ProjectBuilder.builder().withParent(rootproject).build()
        project.apply plugin: 'eclipseplugin'
        project.eclipseplugin { sourceproject() }
        project.evaluate()

        File srcDir = project.file("src")
        File srcGenDir = project.file("src-gen")
        DefaultSourceSet sourceSetMain = project.sourceSets.main
        Assert.assertTrue (sourceSetMain.allJava.srcDirs.contains(srcDir))
        Assert.assertTrue (sourceSetMain.allJava.srcDirs.contains(srcGenDir))

        DefaultSourceSet sourceSetTest = project.sourceSets.test
        Assert.assertFalse (sourceSetTest.allJava.srcDirs.contains(srcGenDir))
        Assert.assertFalse (sourceSetTest.allJava.srcDirs.contains(srcDir))
    }

    @Test
    public void testProject () {

        final String OTHERPROJECT_NAME = "someOtherProject"
        ProjectInternal rootproject = ProjectBuilder.builder().build()
        rootproject.apply plugin: 'eclipsebase'
        Project otherProject = ProjectBuilder.builder().withParent(rootproject).withName(OTHERPROJECT_NAME).build()
        otherProject.apply plugin: 'java'
        ProjectInternal project = ProjectBuilder.builder().withParent(rootproject).build()
        project.apply plugin: 'eclipseplugin'
        project.eclipseplugin { testproject(OTHERPROJECT_NAME) }
        project.evaluate()

        File srcDir = project.file("src")
        File srcGenDir = project.file("src-gen")
        DefaultSourceSet sourceSetMain = project.sourceSets.main
        Assert.assertFalse (sourceSetMain.allJava.srcDirs.contains(srcDir))
        Assert.assertFalse (sourceSetMain.allJava.srcDirs.contains(srcGenDir))

        DefaultSourceSet sourceSetTest = project.sourceSets.test
        Assert.assertTrue (sourceSetTest.allJava.srcDirs.contains(srcGenDir))
        Assert.assertTrue (sourceSetTest.allJava.srcDirs.contains(srcDir))

    }

    @Test (expected = IllegalStateException)
    public void sourceAndTestProject () {
        final String OTHERPROJECT_NAME = "someOtherProject"
        ProjectInternal rootproject = ProjectBuilder.builder().build()
        ProjectBuilder.builder().withParent(rootproject).withName(OTHERPROJECT_NAME)build()
        ProjectInternal project = ProjectBuilder.builder().withParent(rootproject).build()

        project.apply plugin: 'eclipseplugin'
        project.eclipseplugin {
            sourceproject ()
            testproject(OTHERPROJECT_NAME)
        }
    }

    @Test (expected = NullPointerException)
    public void testProjectWithNullReference () {
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'eclipseplugin'
        project.eclipseplugin {
            testproject()
        }

    }
}
