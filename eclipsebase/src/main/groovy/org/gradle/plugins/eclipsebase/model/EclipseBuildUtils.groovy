package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.file.FileCollection

import java.util.logging.Level

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 19.06.13
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class EclipseBuildUtils {

    public static FileCollection createClasspath (final Project project) {

        FileCollection collection = project.sourceSets.main.compileClasspath
        collection += project.files (project.sourceSets.main.java.srcDirs)
        collection += project.buildscript.configurations.classpath

        Eclipse eclipseDsl = project.extensions.findByName("eclipseplugin") as Eclipse
        eclipseDsl.allPluginPaths.each {
            collection += project.files(new File (it, "build/classes/main"))
            collection += project.files(new File (it, "src"))
            collection += project.files(new File (it, "src-gen"))
            collection += project.files(it)
        }

        if (log.isLoggable(Level.FINEST))
          collection.each {log.finest("Complete Classpath of target " + getClass().name + ": " + it)}


        return collection
    }
}
