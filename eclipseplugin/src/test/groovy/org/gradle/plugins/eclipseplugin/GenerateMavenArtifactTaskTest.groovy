package org.gradle.plugins.eclipseplugin

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.publish.Publication
import org.gradle.api.publish.PublicationContainer
import org.gradle.plugins.eclipsebase.EclipseBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Test

import java.util.jar.JarFile
import java.util.jar.JarInputStream

class GenerateMavenArtifactTaskTest {

  private void compileLib (final Project project, final String lib, final String... contents) {


    File jarFile = project.file ('build/deps/' + lib)
    JarBuilder jarBuilder = JarBuilder.create().withFile(jarFile)
    contents.each {jarBuilder = jarBuilder.withContent(it)}
    jarBuilder.finish()

    project.dependencies {
      compile project.files (jarFile.absolutePath)
    }
  }

  @Test
  public void provideMavenArtifact () {
    ProjectInternal project = ProjectBuilder.builder().build()
    project.plugins.apply('java')
    compileLib(project, 'org.eclipse.xtext_2.16.0.v20181203-0514.jar', 'README.md', 'log4j.properties')
    compileLib(project, 'org.eclipse.xtext.xtext.ui_2.16.0.v20181203-1056.jar', 'README.md')
    compileLib(project, 'org.eclipse.xtext.ui.codemining.source_2.16.0.v20181203-1056.jar', 'README.md')

    project.plugins.apply(EclipsePluginPlugin)
    project.eclipseplugin {
      mavenize ('xtext', 'org.mygroup', 'org.eclipse.xtext')
    }

    project.evaluate()
    GenerateMavenArtifactTask generateMavenArtifactTask = project.tasks.generateMavenArtifactXtext
    generateMavenArtifactTask.mavenizeArtifact()
    Assert.assertEquals ("xtext-2.16.0.v20181203-0514.jar", generateMavenArtifactTask.archiveName)
    Assert.assertTrue ("Classifier is not empty", generateMavenArtifactTask.classifier.trim().isEmpty())

    println project.projectDir.absolutePath
    PublicationContainer publicationContainer = project.publishing.publications
    Publication publication = publicationContainer.findByName('xtext')
    Assert.assertEquals ("org.mygroup", publication.groupId)
    Assert.assertEquals ("xtext", publication.artifactId)
    Assert.assertEquals ("2.16.0.v20181203-0514", publication.version)
    String archiveName = generateMavenArtifactTask.archiveName
    File jarFileAsFile = project.file ('build/libs/'+ archiveName)
    JarFile jarFile = new JarFile(jarFileAsFile)
    Assert.assertNotNull ("Jar $jarFileAsFile.absolutePath must contain excluded log4j.properties", jarFile.getJarEntry("log4j.properties"))
  }

  @Test
  public void provideMavenArtifactExcludes () {
    ProjectInternal project = ProjectBuilder.builder().build()
    project.plugins.apply('java')
    compileLib(project, 'org.eclipse.xtext_2.16.0.v20181203-0514.jar', 'README.md', 'log4j.properties')

    project.plugins.apply(EclipsePluginPlugin)
    project.eclipseplugin {
      mavenize ('xtext', 'org.mygroup', 'org.eclipse.xtext', 'log4j.properties')
    }

    project.evaluate()
    GenerateMavenArtifactTask generateMavenArtifactTask = project.tasks.generateMavenArtifactXtext
    generateMavenArtifactTask.mavenizeArtifact()
    String archiveName = generateMavenArtifactTask.archiveName
    File jarFileAsFile = project.file ('build/libs/'+ archiveName)
    JarFile jarFile = new JarFile(jarFileAsFile)
    Assert.assertNull ("Jar $jarFileAsFile.absolutePath must not contain excluded log4j.properties", jarFile.getJarEntry("log4j.properties"))

  }
}
