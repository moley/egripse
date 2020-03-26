package org.gradle.plugins.eclipseplugin

import groovy.util.logging.Slf4j
import org.gradle.api.file.FileTree
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.eclipsebase.dsl.MavenizeItem

@Slf4j
class GenerateMavenArtifactTask extends Jar {

    MavenizeItem mavenizeItem


    @TaskAction
    public void mavenizeArtifact() {
        log.info "Use " + mavenizeItem.name
        FileTree jarFile = project.zipTree(mavenizeItem.jarFile.absolutePath)
        from(jarFile)

        if (!mavenizeItem.excludes.isEmpty())
            exclude(mavenizeItem.excludes)
        getArchiveFileName().set(mavenizeItem.name + "-" + mavenizeItem.version + ".jar")
        log.info "Build archive ${archiveFileName.get()})"

        project.file('build/libs').mkdirs()
        super.copy()
    }
}
