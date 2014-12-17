import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.DefaultSourceSet
import org.gradle.api.tasks.SourceSet
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test

import java.nio.file.Files

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
        Assert.assertFalse (sourceSetMain.allJava.srcDirs.contains(srcGenDir))

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
        File srcDir = project.file("src")
        File srcGenDir = project.file("src-gen")
        Assert.assertTrue (srcDir.mkdirs())
        project.apply plugin: 'eclipseplugin'
        project.eclipseplugin { testproject(OTHERPROJECT_NAME) }
        project.evaluate()


        DefaultSourceSet sourceSetMain = project.sourceSets.main
        Assert.assertTrue (sourceSetMain.allJava.srcDirs.contains(srcDir))
        Assert.assertFalse (sourceSetMain.allJava.srcDirs.contains(srcGenDir))
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

    @Test
    public void mavenProject () {
        final String MAINJAVA = 'src/main/java'
        final String TESTJAVA = 'src/test/java'
        File projectDir = Files.createTempDirectory("mavenProject").toFile()
        File srcMain = new File (projectDir, MAINJAVA)
        File testMain = new File (projectDir, TESTJAVA)
        Assert.assertTrue ("SrcMain could not be created", srcMain.mkdirs())
        Assert.assertTrue ("SrcMain could not be created", testMain.mkdirs())

        ProjectInternal project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        project.apply plugin: 'eclipseplugin'
        project.evaluate()
        SourceSet sourceSetMain = project.sourceSets.main
        File srcMainFromProject = project.file(MAINJAVA)
        Assert.assertTrue ("$srcMainFromProject not contained in sourceset main ($sourceSetMain.allJava.srcDirs)", sourceSetMain.allJava.srcDirs.contains(srcMainFromProject))

        SourceSet sourceSetTest = project.sourceSets.test
        File srcTestFromProject = project.file(TESTJAVA)
        Assert.assertTrue ("$srcTestFromProject not contained in sourceset test ($sourceSetTest.allJava.srcDirs)\"", sourceSetTest.allJava.srcDirs.contains(srcTestFromProject))

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
