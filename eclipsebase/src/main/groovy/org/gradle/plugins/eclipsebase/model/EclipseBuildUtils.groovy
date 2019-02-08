package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 19.06.13
 * Time: 14:00
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class EclipseBuildUtils {

    public static final String eclipsemodelName = "eclipsemodel"


    public static String determineVersionFromJarFile (final File jarfile) {
        String [] tokens = jarfile.name.split("_")
        if (tokens.length < 2)
            throw new IllegalStateException("Jarfile " + jarfile.absolutePath + " does not contain version")
        return tokens.last().replace(".jar", "")
    }

    public static File findDependency (final Project project, final String mavenizeItem) {
        File foundJarFile = null

        for (File next : project.configurations.compile) {
            if (next.name.startsWith(mavenizeItem + "_"))
                return next
        }

        if (foundJarFile == null)
            throw new IllegalStateException("No matching dependency found for identifier " + mavenizeItem)
    }

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


        return collection
    }

    /**
     * creates a eclipsemodel on the rootproject if not yet done
     * or returns the cretaed eclipsemodel
     * @param project  current project
     * @return model
     */
    public static Eclipse ensureModel (final Project project) {
        Project rootproject = project.rootProject
        Eclipse eclipsemodel = getModel(rootproject)
        if (eclipsemodel == null)
          eclipsemodel = rootproject.extensions.create("eclipsemodel", Eclipse, project)

        return eclipsemodel
    }

    public static Eclipse getModel (final Project project) {
        return project.rootProject.extensions.findByName(eclipsemodelName)
    }
}
