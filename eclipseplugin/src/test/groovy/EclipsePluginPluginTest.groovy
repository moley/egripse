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

    private final String MAINJAVA = 'src/main/java'
    private final String TESTJAVA = 'src/test/java'

    private final String MAINRESOURCES = 'src/main/resources'
    private final String TESTRESOURCES = 'src/test/resources'

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
        Assert.assertTrue ("Could not create sourcedir $srcDir.absolutePath", srcDir.mkdirs())
        File srcGenDir = project.file("src-gen")

        project.apply plugin: 'eclipseplugin'
        project.eclipseplugin { testproject(OTHERPROJECT_NAME) }
        project.evaluate()


        DefaultSourceSet sourceSetMain = project.sourceSets.main
        Assert.assertTrue ("$srcDir not removed ($sourceSetMain.allJava.srcDirs)", sourceSetMain.allJava.srcDirs.isEmpty())
        Assert.assertFalse ("$srcGenDir not contained in sourceset test ($sourceSetMain.allJava.srcDirs)", sourceSetMain.allJava.srcDirs.contains(srcGenDir))
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

        SourceSet sourceSetMain = project.sourceSets.main
        File srcMainFromProject = project.file(MAINJAVA)
        File plainSrc = project.file ("src")
        Assert.assertFalse ("$srcMainFromProject not contained in sourceset main ($sourceSetMain.allJava.srcDirs)", sourceSetMain.allJava.srcDirs.contains(srcMainFromProject))
        Assert.assertTrue ("$srcMainFromProject not contained in sourceset test ($sourceSetMain.allJava.srcDirs)\"", sourceSetMain.allJava.srcDirs.contains(plainSrc))

        SourceSet sourceSetTest = project.sourceSets.test
        File srcTestFromProject = project.file(TESTJAVA)
        Assert.assertFalse ("$srcTestFromProject not contained in sourceset test ($sourceSetTest.allJava.srcDirs)\"", sourceSetTest.allJava.srcDirs.contains(srcTestFromProject))
    }

    @Test
    public void mavenProject () {

        File projectDir = Files.createTempDirectory("mavenProject").toFile()
        Assert.assertTrue ("javamain could not be created", new File (projectDir, MAINJAVA).mkdirs())
        Assert.assertTrue ("javatest could not be created", new File (projectDir, TESTJAVA).mkdirs())
        Assert.assertTrue ("resourcesmain could not be created", new File (projectDir, MAINRESOURCES).mkdirs())
        Assert.assertTrue ("resourcestest could not be created", new File (projectDir, TESTRESOURCES).mkdirs())

        ProjectInternal project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        project.apply plugin: 'eclipseplugin'
        project.evaluate()
        SourceSet sourceSetMain = project.sourceSets.main
        File javaMainFromProject = project.file(MAINJAVA)
        File resourcesMainFromProject = project.file(MAINRESOURCES)
        File plainSrc = project.file ("src")
        Assert.assertTrue ("$javaMainFromProject not contained in sourceset main ($sourceSetMain.allJava.srcDirs)", sourceSetMain.allJava.srcDirs.contains(javaMainFromProject))
        Assert.assertTrue ("$resourcesMainFromProject not contained in sourceset main ($sourceSetMain.allJava.srcDirs)", sourceSetMain.resources.srcDirs.contains(resourcesMainFromProject))
        Assert.assertFalse ("$javaMainFromProject not contained in sourceset test ($sourceSetMain.resources.srcDirs)\"", sourceSetMain.allJava.srcDirs.contains(plainSrc))

        SourceSet sourceSetTest = project.sourceSets.test
        File javaTestFromProject = project.file(TESTJAVA)
        File resourcesTestFromProject = project.file(TESTRESOURCES)

        Assert.assertTrue ("$javaTestFromProject not contained in sourceset test ($sourceSetTest.allJava.srcDirs)\"", sourceSetTest.allJava.srcDirs.contains(javaTestFromProject))
        Assert.assertTrue ("$resourcesTestFromProject not contained in sourceset test ($sourceSetTest.resources.srcDirs)\"", sourceSetTest.resources.srcDirs.contains(resourcesTestFromProject))
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
