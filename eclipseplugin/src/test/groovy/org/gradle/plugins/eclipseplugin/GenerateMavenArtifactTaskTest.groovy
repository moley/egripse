package org.gradle.plugins.eclipseplugin

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.plugins.eclipsebase.EclipseBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GenerateMavenArtifactTaskTest {

  private void compileLib (final Project project, final String lib) {


    File jarFile = project.file ('build/deps/' + lib)
    JarBuilder.create().withFile(jarFile).withContent("README.md").finish()

    project.dependencies {
      compile project.files (jarFile.absolutePath)
    }
  }

  @Test
  public void provideMavenArtifact () {
    ProjectInternal project = ProjectBuilder.builder().build()
    project.plugins.apply('java')

    compileLib(project, 'org.eclipse.xtext_2.16.0.v20181203-0514.jar')
    compileLib(project, 'org.eclipse.xtext.xtext.ui_2.16.0.v20181203-1056.jar')
    compileLib(project, 'org.eclipse.xtext.ui.codemining.source_2.16.0.v20181203-1056.jar')

    project.plugins.apply(EclipseBasePlugin)
    project.eclipsebase {
      mavenize ('xtext', 'org.mygroup', 'org.eclipse.xtext')
    }

    project.evaluate()
    GenerateMavenArtifactTask generateMavenArtifactTask = project.tasks.generateMavenArtifactXtext
    generateMavenArtifactTask.mavenizeArtifact()

    println project.projectDir.absolutePath

  }
}
