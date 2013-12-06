package org.gradle.plugins.eclipseplugin

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.eclipsebase.config.LayoutConfigurator
import org.gradle.plugins.eclipsebase.config.SynchronizeBuildMetadata
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.EclipseBuildUtils
import org.gradle.plugins.eclipseplugin.model.EclipsePluginDsl
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.model.Classpath
import org.gradle.plugins.ide.eclipse.model.ClasspathEntry
import org.gradle.plugins.ide.eclipse.model.EclipseModel
import org.gradle.plugins.ide.eclipse.model.Library

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 10:01
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class EclipsePluginPlugin implements Plugin<Project>  {

    public final static String TASKNAME_CONFIGURE_BUILD = "configureBuild"

    public final static String TASKNAME_MIRROR_DEPENDENCIES = "mirrorDependencies"

    private LayoutConfigurator layoutconfigurator = new LayoutConfigurator()


    @Override
    void apply(Project project) {
        log.info ("Applying plugin ${getClass()} in project ${project.name}")

        project.plugins.apply(JavaPlugin) //We need for compile configuration
        project.plugins.apply(EclipsePlugin)

        /**Eclipse eclipseModel = project.rootProject.extensions.eclipsemodel
        EclipsePlugin currentEclipseplugin = eclipseModel.workspace.findPluginByPath(nextSubProject.projectDir)
        if (currentEclipseplugin != null) {
            MetaInf metaInf = currentEclipseplugin.metainf
            if (metaInf == null)
              log.warn("Project " + project.name + " has no META-INF/MANIFEST.MF file")
        }  **/

        SynchronizeBuildMetadata syncBuildproperties = project.tasks.create(type:SynchronizeBuildMetadata, name:SynchronizeBuildMetadata.TASKNAME_SYNC_BUILD_METADATA)
        project.tasks.processResources.dependsOn syncBuildproperties

        EclipsePluginDsl plugindsl = project.extensions.create("eclipseplugin", EclipsePluginDsl, project)

        DefaultTask javaTask = project.tasks.findByName("compileJava")
        if (javaTask == null) throw new IllegalStateException("compileJava not available " + project.tasks.toListString())

        ConfigurePluginProjectTask configureBuildTask = project.tasks.create(type:ConfigurePluginProjectTask, name:TASKNAME_CONFIGURE_BUILD)
        javaTask.dependsOn configureBuildTask

        project.afterEvaluate {
          if (plugindsl.mirrorDependencies) {
            log.info("Adding task ${TASKNAME_MIRROR_DEPENDENCIES}")
            MirrorDependenciesTask mirrorDepsTask = project.tasks.create(type:MirrorDependenciesTask, name:TASKNAME_MIRROR_DEPENDENCIES)
            project.tasks.classes.dependsOn(mirrorDepsTask)
          }
          else
            log.info("No task ${TASKNAME_MIRROR_DEPENDENCIES} added")
        }

        configureProjectFiles(project)

        project.afterEvaluate { //because we need configuration at eclipseplugin to know what to do, must be configured before created compile tasks
          configureProjectLayout(project, plugindsl)
          disableTestsBreakingTheBuild(project)
        }
    }

    void configureProjectFiles (final Project project) {

        EclipseModel eclipsemodel = project.extensions.findByType(EclipseModel)
        eclipsemodel.project {
                natures 'org.eclipse.pde.PluginNature'
                buildCommand 'org.eclipse.pde.ManifestBuilder'
                buildCommand 'org.eclipse.pde.SchemaBuilder'
            }

        Eclipse eclipseModel = EclipseBuildUtils.ensureModel(project)

        Collection<ClasspathEntry> toRemove = new ArrayList<ClasspathEntry>()
        eclipsemodel.classpath {
                file.whenMerged { Classpath classpath ->

                    classpath.entries.each {
                        ClasspathEntry classpathEntry ->

                            if (classpathEntry instanceof Library) {
                               Library lib = classpathEntry as Library
                               if (lib.library.file.absolutePath.startsWith(eclipseModel.explodedTargetplatformPath.absolutePath)) {
                                   log.info("Remove library " + lib.library.file.absolutePath + " from classpath")
                                   toRemove.add(lib)
                               }
                            }
                    }

                    classpath.entries.removeAll(toRemove)

                   // Container container = new Container("org.eclipse.pde.core.requiredPlugins")
                   // classpath.entries.add(container)
                }

                }
    }



    public void disableTestsBreakingTheBuild (final Project project) {
        project.gradle.taskGraph.whenReady { taskGraph ->
            project.tasks.withType(Test).each { Test test ->
                test.ignoreFailures = true
            }
        }
    }




    public void configureProjectLayout (final Project project, final EclipsePluginDsl plugindsl) {
        log.info("Configure projectlayout for project ${project.name}")

        if (plugindsl.sourceproject) {
            log.info("Configure projectlayout for project ${project.name} as sourceproject")
            project.sourceSets {
                main {
                    java {
                        srcDirs = ["src", "src-gen"]
                    }
                }
            }
        }

        if (plugindsl.testprojectFor != null) {
            log.info("Configure projectlayout for project ${project.name} as testproject")
            project.sourceSets {
                test {
                    java {
                        srcDirs = ["src", "src-gen"]
                    }
                }
            }

        }

        layoutconfigurator.configure(project)



        //log.info(" - sourcedirs main : " + project.sourceSets.main.java.srcDirs)
        //log.info(" - sourcedirs test : " + project.sourceSets.test.java.srcDirs)
    }
}
