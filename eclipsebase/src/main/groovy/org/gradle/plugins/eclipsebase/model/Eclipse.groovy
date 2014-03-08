package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j
import org.gradle.api.Project
import org.gradle.plugins.eclipsebase.dsl.EclipseBaseDsl

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 11:59
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class Eclipse {



    Targetplatform targetplatformModel

    Workspace workspace

    private final static String USER_HOME = System.getProperty("user.home")

    private final HashMap<MetaInf, Collection<Dependency>> dependenciesCache = new HashMap<MetaInf, Collection<Dependency>>()


    private Project project

    private EclipseBaseDsl eclipseBaseDsl

    Collection <DefaultPluginContainer> pluginContainers

    File explodedTargetplatform

    public Eclipse(final Project project) {
        this.project = project
    }

    public EclipseBaseDsl getEclipseDsl () {
        if (this.eclipseBaseDsl == null)
          this.eclipseBaseDsl = project.rootProject.extensions.findByName("eclipsebase")

        return this.eclipseBaseDsl
    }

    public File getLocalUpdatesitePath () {
        return project.rootProject.file("build/updatesite")
    }

    public File getLocalUpdatesiteContentPath () {
        return project.rootProject.file('build/newUpdatesiteContent')
    }


    public File getCacheDirectory() { //TODO -> nach .gradle verschieben?
        return new File(USER_HOME, ".egripse")
    }

    public Workspace getWorkspace () {
        if (this.workspace == null) {
            String pluginsPath = eclipseDsl.pluginsPath
            String featuresPath = eclipseDsl.featuresPath
            this.workspace = new Workspace(project, pluginsPath, featuresPath)
        }

        return this.workspace
    }

    public Collection <DefaultPluginContainer> getPluginContainers () {
        if (this.pluginContainers == null) {
            this.pluginContainers = new ArrayList<DefaultPluginContainer>()
            this.pluginContainers.add(getTargetplatformModel())
            this.pluginContainers.add(getWorkspace())
            this.pluginContainers.addAll(getAdditionalLocalUpdatesites())
        }

        return this.pluginContainers
    }

    public File getExplodedTargetplatformPath () {
        return explodedTargetplatform
    }

    public Targetplatform getTargetplatformModel () {
        final String targetplatform = eclipseDsl.targetplatform

        if (targetplatform == null)
            throw new IllegalStateException("No targetplatform defined")

        if (this.targetplatformModel == null) {
            downloadTargetplatformOnDemand()
            log.info("Read targetplatform for project " + project.name + " in path "+ eclipseDsl.againstEclipse)
            this.targetplatformModel = new Targetplatform(project, new File(eclipseDsl.againstEclipse))
            log.info("Finished createing targetplatform")
        }
        else
            log.info("Targetplatform is already created")

        return targetplatformModel

    }

    public List<Targetplatform> getAdditionalLocalUpdatesites () {
        List<Targetplatform> platforms = new ArrayList<Targetplatform>()

        if (eclipseDsl.additionalLocalUpdatesites != null) {
            for (String next: eclipseDsl.additionalLocalUpdatesites) {
              File nextPath = project.file (next)
              if (! nextPath.exists())
                throw new IllegalStateException("Additional local updatesite ${nextPath.absolutePath} does not exist")

              platforms.add(new Targetplatform(project, project.file(next)))
            }

        }

        return platforms

    }


    private void downloadTargetplatformOnDemand() {

        if (!cacheDirectory.exists())
            cacheDirectory.mkdirs()

        final String targetplatform = eclipseDsl.targetplatform
        String[] targetplatformtokens = targetplatform.split("/")

        String targetplatformName = targetplatformtokens[targetplatformtokens.length - 1]

        File downloadedTargetplatform = new File(cacheDirectory, targetplatformName)
        explodedTargetplatform = new File(cacheDirectory, targetplatformName + "_exploded")

        if (!explodedTargetplatform.exists()) {
            if (!downloadedTargetplatform.exists()) {
                println("Downloading targetplatform " + downloadedTargetplatform.absolutePath + " from " + targetplatform)
                def out = new BufferedOutputStream(new FileOutputStream(downloadedTargetplatform))
                out << new URL(targetplatform).openStream()
                out.close()
            }

            def ant = new AntBuilder()   // create an antbuilder
            ant.unzip(src: downloadedTargetplatform.absolutePath,
                    dest: explodedTargetplatform.absolutePath,
                    overwrite: "true")

        }


        log.info("Using exploded targetplatform in ${explodedTargetplatform.absolutePath}")
        eclipseDsl.againstEclipse = explodedTargetplatform.absolutePath + File.separator + "eclipse"
    }



    public void log () {
        for (MetaInf nextMetaInf: dependenciesCache.keySet()) {
            Collection<Dependency> deps = dependenciesCache.get(nextMetaInf)

            log.info("Cache of metainf " + nextMetaInf.bundleID + "(" + System.identityHashCode(nextMetaInf) + ":")
            logDependencies("", deps)
        }
    }
    public void logDependencies (final String pre, final Collection<Dependency> deps) {
        for (Dependency nextDep: deps) {
            String resolvedString = nextDep.resolved ? nextDep.resolvedPlugin.originPath.name : "not resolved"
            log.info(pre + "  - " + nextDep.bundleID + ", " + resolvedString + ", " + System.identityHashCode(nextDep))
            EclipsePlugin depPlugin = nextDep.resolvedPlugin
            if (depPlugin != null)
                logDependencies(pre + "  ", depPlugin.dependencies)
        }
    }

    public Collection<Dependency> getDependencies(final MetaInf metaInf) {
        log.info("get external dependencies of metainf file " + metaInf.bundleID + "(" + System.identityHashCode(metaInf) + ")")

        Collection <Dependency> cachedDeps = dependenciesCache.get(metaInf)
        if (cachedDeps != null) {
            log.info("get external dependencies of bundle " + metaInf.bundleID + " from cache")
            return cachedDeps
        }


        cachedDeps = new ArrayList<EclipsePlugin>()

        for (Dependency nextDep : metaInf.dependencies) {
            EclipsePlugin foundPlugin = targetplatformModel.findPluginByBundleID(nextDep.bundleID)
            if (foundPlugin == null)
                cachedDeps.add(nextDep)
        }

        dependenciesCache.put(metaInf, cachedDeps)

        log.info("get external dependencies of bundle " + metaInf.bundleID + " from targetplatform (metainf ${System.identityHashCode(metaInf)})")

        return cachedDeps
    }


}
