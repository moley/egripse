package org.gradle.plugins.eclipseplugin

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.eclipsebase.config.LayoutConfigurator
import org.gradle.plugins.eclipsebase.config.SynchronizeBuildMetadata
import org.gradle.plugins.eclipsebase.dsl.MavenizeItem
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.EclipseBuildUtils
import org.gradle.plugins.eclipseplugin.model.EclipsePluginDsl
import org.gradle.plugins.ide.eclipse.EclipsePlugin
import org.gradle.plugins.ide.eclipse.model.*

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 10:01
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class EclipsePluginPlugin implements Plugin<Project> {

  public final static String TASKNAME_CONFIGURE_BUILD = "configureBuild"

  public final static String TASKNAME_MIRROR_DEPENDENCIES = "mirrorDependencies"

  private LayoutConfigurator layoutconfigurator = new LayoutConfigurator()


  @Override
  void apply(Project project) {
    log.info("Applying plugin ${getClass()} in project ${project.name}")

    project.plugins.apply(JavaPlugin) //We need for compile configuration
    project.plugins.apply(EclipsePlugin)

    SynchronizeBuildMetadata syncBuildproperties = project.tasks.create(type: SynchronizeBuildMetadata, name: SynchronizeBuildMetadata.TASKNAME_SYNC_BUILD_METADATA)
    project.tasks.processResources.dependsOn syncBuildproperties

    EclipsePluginDsl plugindsl = project.extensions.create("eclipseplugin", EclipsePluginDsl, project)

    DefaultTask javaTask = project.tasks.findByName("compileJava")
    if (javaTask == null) throw new IllegalStateException("compileJava not available " + project.tasks.toListString())

    ConfigurePluginProjectTask configureBuildTask = project.tasks.create(type: ConfigurePluginProjectTask, name: TASKNAME_CONFIGURE_BUILD)
    javaTask.dependsOn configureBuildTask

    project.afterEvaluate {
      if (plugindsl.mirrorDependencies) {
        log.info("Adding task ${TASKNAME_MIRROR_DEPENDENCIES}")
        MirrorDependenciesTask mirrorDepsTask = project.tasks.create(type: MirrorDependenciesTask, name: TASKNAME_MIRROR_DEPENDENCIES)
        project.tasks.classes.dependsOn(mirrorDepsTask)
      } else
        log.info("No task ${TASKNAME_MIRROR_DEPENDENCIES} added")

      for (MavenizeItem next : plugindsl.mavenizeItems) {
        project.plugins.apply 'maven-publish'

        File jarFile = EclipseBuildUtils.findDependency(project, next.origin)
        String jarversion = EclipseBuildUtils.determineVersionFromJarFile(jarFile)
        next.version = jarversion
        next.jarFile = jarFile
        GenerateMavenArtifactTask generateMavenArtifactTask = project.tasks.create("generateMavenArtifact" + next.name.capitalize(), GenerateMavenArtifactTask)
        generateMavenArtifactTask.classifier = next.name
        project.tasks.publish.dependsOn generateMavenArtifactTask
        project.tasks.publishToMavenLocal.dependsOn generateMavenArtifactTask

        println "Task " + generateMavenArtifactTask.name + " created for uploading mavenized " + next.origin
        generateMavenArtifactTask.mavenizeItem = next

        PublishingExtension publishingExtension = project.publishing
        publishingExtension.publications.create(next.name, MavenPublication) {
          groupId = next.group
          artifactId = next.name
          version = jarversion

          artifact generateMavenArtifactTask
        }
      }
    }

    configureProjectFiles(project)

    project.afterEvaluate {
      //because we need configuration at eclipseplugin to know what to do, must be configured before created compile tasks
      configureProjectLayout(project, plugindsl)
      disableTestsBreakingTheBuild(project)
    }

    configureDeletablePath(plugindsl, project)
  }

  void configureDeletablePath(final EclipsePluginDsl plugindsl, final Project project) {
    if (!plugindsl.additionalCleanablePath.isEmpty()) {
      log.info("Recoginzed additionCleanablepaths " + plugindsl.additionalCleanablePath)

      Delete taskClean = project.tasks.findByName("clean")
      taskClean.outputs.upToDateWhen {
        for (String nextRemovable : plugindsl.additionalCleanablePath) {
          File nextFile = project.file(nextRemovable)
          if (nextFile.exists()) {
            log.info("Uptodatecheck set to false due to " + nextFile.absolutePath)
            return false
          }
        }
        return true
      }
      taskClean.doFirst {
        for (String next : plugindsl.additionalCleanablePath) {
          delete project.file(next)
        }
      }
    }
  }

  void configureProjectFiles(final Project project) {
    log.info("Configure projectfiles in project " + project.name)

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

        final String REQUIREDPLUGINS_KIND = 'org.eclipse.pde.core.requiredPlugins'

        boolean requiredPluginsContainerAvailable = false

        classpath.entries.each {
          ClasspathEntry classpathEntry ->

            if (classpathEntry instanceof Library) {

              Library lib = classpathEntry as Library
              log.info("Found library " + lib.toString())
              if (lib.library.file.absolutePath.startsWith(eclipseModel.targetplatformModel.path.absolutePath)) {
                log.info("Remove from classpath targetplatform library " + lib.library.file.absolutePath)
                toRemove.add(lib)
              }

              if (!lib.library.file.exists()) {
                log.info("Remove from classpath non existing library " + lib.library.file.absolutePath)
                toRemove.add(lib)
              }

            }
            if (classpathEntry instanceof Container) {
              log.info("Found container " + classpathEntry.toString())
              Container container = classpathEntry
              if (container.path.equals(REQUIREDPLUGINS_KIND)) {
                requiredPluginsContainerAvailable = true
                log.info("Found requiredplugins container")
              } else
                log.info("Container is not requiredplugins container")
            }
            if (classpathEntry instanceof SourceFolder) {
              //Check if this path must be added as sourcefolder
              SourceFolder sourcefolder = classpathEntry
              log.info("Found sourcefolder " + classpathEntry.toString())

              File absolute = sourcefolder.dir != null ? sourcefolder.dir : project.file(sourcefolder.path)

              if (absolute.absolutePath.endsWith("build/mergedResources")) {
                log.info("Remove from classpath mergedresources " + absolute.absolutePath)
                toRemove.add(sourcefolder)
              }
              if (!absolute.exists()) {
                log.info("Remove from classpath non existing sourcepath " + absolute.absolutePath)
                toRemove.add(sourcefolder)
              }
            }
        }

        classpath.entries.removeAll(toRemove)

        if (!requiredPluginsContainerAvailable) {
          Container container = new Container("org.eclipse.pde.core.requiredPlugins")
          classpath.entries.add(container)
        }
      }

    }
  }


  public void disableTestsBreakingTheBuild(final Project project) {
    project.gradle.taskGraph.whenReady { taskGraph ->
      project.tasks.withType(Test).each { Test test ->
        test.ignoreFailures = true
      }
    }
  }


  public void configureProjectLayout(final Project project, final EclipsePluginDsl plugindsl) {
    log.info("Configure projectlayout for project ${project.name}")

    if (plugindsl.testprojectFor != null) {
      log.info("Project is configured as testproject")
      if (!project.file("src/test/java").exists()) {
        log.info("No maven layout found, configuring src as org.eclipse.equinox.simpleconfigurator path and resources as resources path")
        project.sourceSets {
          main {
            java { srcDirs = [] }
            resources { srcDirs = [] }
          }
        }
        project.sourceSets {
          test {
            java { srcDirs = ["src"] }
            resources { srcDirs = ["resources"] }
          }
        }
      } else
        log.info("Maven layout found")

    } else {
      if (!project.file("src/main/java").exists()) {
        project.sourceSets {
          log.info("No maven layout found, configuring src as org.eclipse.equinox.simpleconfigurator path and resources as resources path")
          main {
            java { srcDirs = ["src"] }
            resources { srcDirs = ["resources"] }
          }
        }
      } else
        log.info("Maven layout found")

    }

    plugindsl.additionalSourceDir.each {
      project.sourceSets.main.java.srcDirs it
    }

    layoutconfigurator.configure(project)

    //log.info(" - sourcedirs main : " + project.sourceSets.main.java.srcDirs)
    //log.info(" - sourcedirs test : " + project.sourceSets.test.java.srcDirs)
  }
}
