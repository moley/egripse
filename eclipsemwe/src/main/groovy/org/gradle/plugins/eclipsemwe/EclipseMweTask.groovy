package org.gradle.plugins.eclipsemwe

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecResult

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 23:57
 * To change this template use File | Settings | File Templates.
 */

@Slf4j
class EclipseMweTask extends SourceTask {


    File mweFile

    File basedir

    @OutputDirectory
    File outputDir


    private boolean isMweNecessary (File projectDir){
        log.info("Checking if mwe is necessary in dir $projectDir")
        File srcgen = new File (projectDir, "src-gen")

        if (projectDir.name.equals("vsa.marvin.mod.persist.gmf")) //Workaround because mod.persist.dsl writes to this
            return true

        if (! srcgen.exists()) {
            log.info(" - yes because path doesnt exist")
            return true
        }

        if (srcgen.listFiles() == null || srcgen.listFiles().length == 0) {
            log.info(" - yes because path is empty")
            return true
        }

        for (File next: srcgen.listFiles()) {
            if (! next.name.startsWith(".")) {
                log.info(" - no because path contains file " + next.absolutePath)
                return false
            }
        }

        log.info(" - yes")

        return true

    }


    @TaskAction
    public void call () {

        source.files.each {log.info("Changed file " + it.absolutePath)}

        FileCollection generatorclasspath = createMweClasspath(project)


        ExecResult result = project.javaexec {

            classpath = generatorclasspath


            EclipseMwe eclipseMweDsl = project.extensions.eclipsemwe

            if (eclipseMweDsl.mweFiles.empty) {
                log.warn("mwe plugin applied but no mwefiles defined")
                return
            }

            if (eclipseMweDsl.mweFiles.size() > 1)
                throw new IllegalStateException("In prototype only one mwefile is supported")

            if (mweFile == null)
                mweFile = project.file (eclipseMweDsl.mweFiles.iterator().next())

            String workflowClassname = "org.eclipse.emf.mwe.core.WorkflowRunner"

            //TODO with urlclassloader and real task called in project path

            main = workflowClassname

            Collection arguments = new ArrayList()
            arguments.add(mweFile.absolutePath)
            args  = arguments

            maxHeapSize = "512M"

        }

        if (result.exitValue != 0)
            throw new IllegalStateException("Error running mwe generator in project " + project.name)
        /**else {
          //TODO Workaround until we know how to set outputdirectory of generator
          project.copy {
            from project.file(basedir.absolutePath + "/bin/src-gen")
            into project.file(basedir.absolutePath + "/src-gen")
          }
        }  **/
    }




    public boolean checkPattern (final ArrayList<String> patterns, final File filename) {
        if (log.isDebugEnabled())
            log.debug("Check filename " + filename.name)
        for (String nextPattern: patterns) {
            if (filename.name.indexOf(nextPattern) >= 0) {
                if (log.isDebugEnabled())
                    log.debug("...pattern " + nextPattern + "matched")

                return true
            }
        }

        return false
    }





    private FileCollection createMweClasspath(final Project currentProject) {
        log.info("Building mweclasspath in project " + currentProject.name)



        //TODO lay generator/xtext and this whole stuff onto the buildscript classpath
        final List<String> okPattern = new ArrayList<String>()
        okPattern.add("org.eclipse.core")
        okPattern.add("org.eclipse.osgi")
        okPattern.add("org.eclipse.equinox")
        okPattern.add("org.eclipse.text")

        okPattern.add("com.ibm.icu_")
        okPattern.add("org.eclipse.xsd_")
        okPattern.add("org.antlr.runtime_")
        okPattern.add("org.antlr.generator_")
        okPattern.add("org.apache.log4j_")
        okPattern.add("org.eclipse.emf")
        okPattern.add("org.apache.commons.logging_")
        okPattern.add("org.apache.commons.cli_")
        okPattern.add("de.itemis.xtext.antlr_")
        okPattern.add("com.google.guava_")
        okPattern.add("com.google.inject_")
        okPattern.add("org.eclipse.jdt.core_")
        okPattern.add("org.eclipse.equinox.common_")
        okPattern.add("org.eclipse.xpand")
        okPattern.add("org.eclipse.xtend")
        okPattern.add("org.eclipse.core.resources")
        okPattern.add("org.eclipse.xtext")
        okPattern.add("org.eclipse.xtext.xbase.lib_")
        okPattern.add("org.eclipse.xtext.activities_")
        okPattern.add("org.eclipse.xtext.common.types_")
        okPattern.add("org.eclipse.xtext.ecore_")
        okPattern.add("org.eclipse.xtext.generator_")
        okPattern.add("org.eclipse.xtext.junit_")
        okPattern.add("org.eclipse.xtext.junit4_")
        okPattern.add("org.eclipse.xtext.logging_")
        okPattern.add("org.eclipse.xtext.util_")
        okPattern.add("org.eclipse.xtext.xtend2_")
        okPattern.add("org.eclipse.xtext.xtend2.lib_")
        okPattern.add("org.eclipse.xtext.xtend2.lib_")
        okPattern.add("javax.inject_")
        okPattern.add("vsa.marvin.mod.build")

        FileCollection collection = currentProject.sourceSets.main.compileClasspath.filter { File file ->
            checkPattern(okPattern, file)
        }

        collection += currentProject.files (currentProject.sourceSets.main.java.srcDirs)
        collection += currentProject.buildscript.configurations.classpath

        addProjectPaths(currentProject, currentProject, collection)
        for (Dependency next: currentProject.configurations.getByName("compile").dependencies.findAll()) {
            if (next instanceof ProjectDependency) {
                addProjectPaths(currentProject, next.dependencyProject, collection)
            }
        }

        log.info ("Check MweClasspath in project " + currentProject.name)

        collection.each {log.info ("Next classpathentry in project " + currentProject.name + ": " + it)}


        return collection

    }

    private void addProjectPaths (final Project currentProject, final Project dependendProject, FileCollection collection) {
        log.info("Configure next dependent project: " + dependendProject + " in project " + currentProject.name)
        addToCollectionIfExists(dependendProject, collection, "build/classes/main")
        addToCollectionIfExists(dependendProject, collection, "bin")
        addToCollectionIfExists(dependendProject, collection, "src")
        addToCollectionIfExists(dependendProject, collection, "src-gen")
    }

    /**
     * adds file to collection be aware of project paths
     * @param project           project
     * @param collection        collection to add
     * @param file              file to be added
     */
    private void addToCollectionIfExists (final Project project, FileCollection collection, final String file) {
        FileCollection added = project.files(file)
        for (File nextFile: added.getFiles() ) {
          if (nextFile.exists()) {
            log.info("Add file " + nextFile.absolutePath + " to classpath of project" + project.name)

            if (added != null)
              collection.add(added)
          }
          else
            log.info("Ignore file " + nextFile.absolutePath + " due to not existing (configuring project " + project.name + ")")
        }
    }

}
