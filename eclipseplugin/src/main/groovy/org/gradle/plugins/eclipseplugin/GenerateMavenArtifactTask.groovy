package org.gradle.plugins.eclipseplugin

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.eclipsebase.dsl.MavenizeItem

class GenerateMavenArtifactTask extends Jar {

  MavenizeItem mavenizeItem


  @TaskAction
  public void mavenizeArtifact () {

    String version
    File foundJarFile = null

    for (File next : project.configurations.compile) {
      if (next.name.startsWith(mavenizeItem.origin + "_")) {
        println "Use " + next.name
        foundJarFile = next
        version = next.name.split("_").last()
        FileTree jarFile = project.zipTree(next.absolutePath)
        from (jarFile)
      }
    }

    if (foundJarFile == null)
      throw new IllegalStateException("No matching dependency found for " + mavenizeItem.origin)

    if (! mavenizeItem.excludes.isEmpty())
      exclude(mavenizeItem.excludes)

    archiveName = mavenizeItem.name + "-" + version
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
