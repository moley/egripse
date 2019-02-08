package org.gradle.plugins.eclipseplugin

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.eclipsebase.dsl.MavenizeItem

class GenerateMavenArtifactTask extends Jar {

  MavenizeItem mavenizeItem


  @TaskAction
  public void mavenizeArtifact () {
    println "Use " + mavenizeItem.name
    FileTree jarFile = project.zipTree(mavenizeItem.jarFile.absolutePath)
    from (jarFile)

    if (! mavenizeItem.excludes.isEmpty())
      exclude(mavenizeItem.excludes)

    archiveName = mavenizeItem.name + "-" + mavenizeItem.version + ".jar"
    println "Build archive $archiveName"





    println "Components:" + project.components

    /**
     * task removeLog4jFromMwe (type: Jar, dependsOn: "mirrorDependencies") {*     archiveName = 'org.eclipse.emf.mwe.core.jar'
     *     excludes = ['log4j.properties', 'log4j.xml', 'META-INF/*.SF', 'META-INF/*.DSA', 'META-INF/*.RSA']
     *     from(zipTree('build/deps/org.eclipse.emf.mwe.core.jar'))
     * }
     */
    project.file('build/libs').mkdirs()
    super.copy()


  }
}
