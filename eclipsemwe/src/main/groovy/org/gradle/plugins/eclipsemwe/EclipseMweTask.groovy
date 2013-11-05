package org.gradle.plugins.eclipsemwe

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.StopExecutionException
import org.gradle.api.tasks.TaskAction

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 23:57
 * To change this template use File | Settings | File Templates.
 */

@Slf4j
class EclipseMweTask extends JavaExec { //extends DefaultTask {


    File mweFile

    File basedir



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
    public void exec () {

        setClasspath(createMweClasspath(project))

        if (! isMweNecessary(basedir))
            throw new StopExecutionException("MWE is not necessary for project " + project.name)

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

        setMain(workflowClassname)

        Collection args = new ArrayList()
        args.add(mweFile.absolutePath)
        setArgs(args)

        setMaxHeapSize("512M")
        super.exec()

        if (!getDidWork())
            throw new IllegalStateException("Error running mwe generator in project " + project.name)
        else {
        //TODO Workaround until we know how to set outputdirectory of generator
        project.copy {
                from project.file(basedir.absolutePath + "/bin/src-gen")
                into project.file(basedir.absolutePath + "/src-gen")

            }
        }
    }


  /**
    @TaskAction
    public void exec () {

        println ("Starting mwegenerator in path " + new File ("").absolutePath)
        String workflowClassname = "org.eclipse.emf.mwe.core.WorkflowRunner"

        JavaClassLauncher launcher = new JavaClassLauncher()
        def workflowrunner = launcher.loadGenerator(classpath, workflowClassname).newInstance()
        def nullprogressmonitor = launcher.loadClassWithUrlClassloader("org.eclipse.emf.mwe.core.monitor.NullProgressMonitor").newInstance()

        String wfFile = mweFile.absolutePath
        Map properties = new HashMap();
        properties.put("baseDir", basedir.absolutePath)
        Map slotContents = new HashMap()
        if (workflowrunner.run(wfFile , nullprogressmonitor, properties, slotContents) == false)
            throw new IllegalStateException("Workflowrunner in path " + basedir.absolutePath + " with mwe file " + mweFile.absolutePath + " returned an error")
    }          **/


    public boolean checkPattern (final ArrayList<String> patterns, final File filename) {
        //println ("Check filename " + filename.name)
        for (String nextPattern: patterns) {
            if (filename.name.indexOf(nextPattern) >= 0) {
                //println ("...pattern matched")

                return true
            }
        }

        return false
    }



    private FileCollection createMweClasspath(final Project project) {
        log.info("Building mweclasspath in project " + project.name)

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
        okPattern.add("org.eclipse.xtext_")
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
        okPattern.add("javax.inject_")
        okPattern.add("vsa.marvin.mod.build")

        FileCollection collection = project.sourceSets.main.compileClasspath.filter { File file ->
            checkPattern(okPattern, file)
        }

        collection += project.files (project.sourceSets.main.java.srcDirs)
        collection += project.buildscript.configurations.classpath

        Project rootproject = project.rootProject

        for (Project next: rootproject.subprojects) {
            log.info("Configure additional paths in " + next.projectDir.absolutePath)
            File projectPath = next.projectDir
            addToCollectionIfExists(project, collection, new File (projectPath, "build/classes/main"))
            addToCollectionIfExists(project, collection, new File (projectPath, "src"))
            addToCollectionIfExists(project, collection, new File (projectPath, "src-gen"))
        }

        log.info ("Check MweClasspath in project " + project.name)

        collection.each {log.info ("Next classpathentry in project " + project.name + ": " + it)}


        return collection

    }

    private void addToCollectionIfExists (final Project project, FileCollection collection, final File file) {

        if (file.exists()) {
            log.info("Add file " + file.absolutePath + " to classpath")
            FileCollection added = project.files(file)
            if (added != null)
              collection.add(added)
        }
        else
            log.info("Ignore file " + file.absolutePath + " due to not existing")
    }

}
