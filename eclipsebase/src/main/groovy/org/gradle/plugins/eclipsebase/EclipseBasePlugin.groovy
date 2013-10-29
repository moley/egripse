package org.gradle.plugins.eclipsebase

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.plugins.eclipsebase.config.BuildPropertiesConfigurator
import org.gradle.plugins.eclipsebase.config.ProjectVersionConfigurator
import org.gradle.plugins.eclipsebase.dsl.EclipseBaseDsl
import org.gradle.plugins.eclipsebase.dsl.UpdatesiteDsl
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.EclipsePlugin
import org.gradle.plugins.eclipsebase.model.MetaInf
import org.gradle.plugins.eclipsebase.updatesite.*

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 25.06.13
 * Time: 15:57
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class EclipseBasePlugin implements Plugin<Project> {

    private static Date currentBuildDate

    private EclipseDependencyResolver dependencyResolver = new EclipseDependencyResolver()

    private ProjectVersionConfigurator projectVersionConfigurator = new ProjectVersionConfigurator()

    private BuildPropertiesConfigurator buildPropertiesConfigurator = new BuildPropertiesConfigurator()

    static {
        currentBuildDate = new Date(System.currentTimeMillis())
    }

    @Override
    void apply(Project project) {
        log.info ("Applying plugin " +getClass() + " in project " + project.name)

        printMemory()

        EclipseBaseDsl eclipseBaseDsl = project.extensions.create("eclipsebase", EclipseBaseDsl, project)
        UpdatesiteDsl updatesite = project.extensions.create("updatesite", UpdatesiteDsl, eclipseBaseDsl)
        eclipseBaseDsl.updatesite = updatesite

        Eclipse eclipseModel = project.extensions.create("eclipsemodel", Eclipse, project)

        project.plugins.apply(JavaPlugin) //Because we need lifecycle task jar

        if (eclipseBaseDsl.updatesite != null)
            configureUpdatesiteTasks(project)

        //dependencies are resolved in afterEvaluate because we need infos from dsl objects
        project.afterEvaluate{

            for (Project nextSubProject: project.rootProject.subprojects) {

                nextSubProject.plugins.apply(JavaPlugin)

                EclipsePlugin currentEclipseplugin = eclipseModel.workspace.findPluginByPath(nextSubProject.projectDir)
                if (currentEclipseplugin != null) {
                  MetaInf metaInf = currentEclipseplugin.metainf
                    if (metaInf == null)
                        log.warn("Project " + nextSubProject.name + " has no META-INF/MANIFEST.MF file")

                  //we have to set version before resolving dependencies
                  if (currentEclipseplugin.buildProperties != null)
                    buildPropertiesConfigurator.synchronizeResourcesFromBuildProperties(nextSubProject, currentEclipseplugin.buildProperties)
                  else
                    log.warn("Project " + nextSubProject.name + " has no build.properties file")

                  projectVersionConfigurator.setVersion(nextSubProject, metaInf.version, metaInf.bundleID)
                }

            }

            Project rootProject = project.rootProject
            dependencyResolver.resolve(rootProject)

        }
    }


    public void configureUpdatesiteTasks (final Project project) {
        project.configurations {
            ftpAntTask
        }

        project.dependencies {
            ftpAntTask("org.apache.ant:ant-commons-net:1.8.4") {
                module("commons-net:commons-net:1.4.1") {
                    dependencies "oro:oro:2.0.8:jar"
                }
            }
        }

        Jar jarTask = project.tasks.findByName("jar")

        //updatesite local Task
        MergeUpdatesiteTask mergeUpdatesiteLocalTask = project.tasks.create("updatesiteMergeLocal", MergeUpdatesiteTask)
        CreateCategoriesUpdatesiteTask createCategoriesUpdatesiteLocalTask = project.tasks.create("updatesiteCategoriesLocal", CreateCategoriesUpdatesiteTask)
        DefaultTask prepareUpdatesiteLocalTask = project.tasks.create ("updatesitePrepareLocal", PrepareFeatureUpdatesiteTask)
        DefaultTask updatesiteLocalTask = project.tasks.create("updatesiteLocal")
        mergeUpdatesiteLocalTask.dependsOn prepareUpdatesiteLocalTask
        createCategoriesUpdatesiteLocalTask.dependsOn (mergeUpdatesiteLocalTask)
        updatesiteLocalTask.dependsOn createCategoriesUpdatesiteLocalTask

        //updatesite Task
        DefaultTask downloadUpdatesiteTask = project.tasks.create("downloadUpdatesite", DownloadUpdatesiteTask)
        CreateCategoriesUpdatesiteTask createCategoriesUpdatesiteTask = project.tasks.create("updatesiteCategories", CreateCategoriesUpdatesiteTask)
        UploadUpdatesiteTask uploadUpdatesiteTask = project.tasks.create("updatesiteUpload", UploadUpdatesiteTask)
        MergeUpdatesiteTask mergeUpdatesiteTask = project.tasks.create("updatesiteMerge", MergeUpdatesiteTask)
        DefaultTask prepareUpdatesiteTask = project.tasks.create ("updatesitePrepare", PrepareFeatureUpdatesiteTask)
        DefaultTask updatesiteTask = project.tasks.create("updatesite")
        prepareUpdatesiteTask.dependsOn jarTask
        mergeUpdatesiteTask.dependsOn downloadUpdatesiteTask
        mergeUpdatesiteTask.dependsOn prepareUpdatesiteTask
        createCategoriesUpdatesiteTask.dependsOn mergeUpdatesiteTask
        uploadUpdatesiteTask.dependsOn createCategoriesUpdatesiteTask
        updatesiteTask.dependsOn uploadUpdatesiteTask
    }

    public void printMemory () {
        int mb = 1024*1024;

        //Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();

        log.info("##### Heap utilization statistics [MB] #####")
        log.info("Used Memory:" + (runtime.totalMemory() - runtime.freeMemory()) / mb)
        log.info("Free Memory:" + runtime.freeMemory() / mb)
        log.info("Total Memory:" + runtime.totalMemory() / mb)
        log.info("Max Memory:" + runtime.maxMemory() / mb)
    }

}
