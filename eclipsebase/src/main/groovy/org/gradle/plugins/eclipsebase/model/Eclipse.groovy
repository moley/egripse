package org.gradle.plugins.eclipsebase.model

import com.diffplug.gradle.oomph.OomphIdeAccessor
import com.diffplug.gradle.oomph.OomphIdeExtension
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
class Eclipse {


  Targetplatform targetplatformModel

  Workspace workspace

  private final String USER_HOME = System.getProperty("user.home")

  private final HashMap<MetaInf, Collection<Dependency>> dependenciesCache = new HashMap<MetaInf, Collection<Dependency>>()


  private Project project

  private EclipseBaseDsl eclipseBaseDsl

  Collection<DefaultPluginContainer> pluginContainers

 public Eclipse(final Project project) {
    this.project = project
  }

  public EclipseBaseDsl getEclipseDsl() {
    if (this.eclipseBaseDsl == null)
      this.eclipseBaseDsl = project.rootProject.extensions.findByName("eclipsebase")

    return this.eclipseBaseDsl
  }

  public File getLocalUpdatesitePath() {
    return project.rootProject.file("build/updatesite")
  }

  public File getLocalUpdatesiteContentPath() {
    return project.rootProject.file('build/newUpdatesiteContent')
  }


  public File getCacheDirectory() { //TODO -> nach .gradle verschieben?
    return new File(USER_HOME, ".egripse")
  }

  public Workspace getWorkspace() {
    if (this.workspace == null) {
      String pluginsPath = eclipseDsl.pluginsPath
      String featuresPath = eclipseDsl.featuresPath
      this.workspace = new Workspace(project, pluginsPath, featuresPath)
    }

    return this.workspace
  }

  public Collection<DefaultPluginContainer> getPluginContainers() {
    if (this.pluginContainers == null) {
      this.pluginContainers = new ArrayList<DefaultPluginContainer>()
      this.pluginContainers.add(getTargetplatformModel())
      this.pluginContainers.add(getWorkspace())
    }

    return this.pluginContainers
  }


  private boolean createProxyFile(final File proxyFile) {

    String httpProxyHost = System.getProperty("http.proxyHost")
    String httpProxyPort = System.getProperty("http.proxyPort")
    String httpBypass = System.getProperty("http.nonProxyHosts")

    String httpsProxyHost = System.getProperty("https.proxyHost")
    String httpsProxyPort = System.getProperty("https.proxyPort")
    String httpsBypass = System.getProperty("https.nonProxyHosts")

    System.out.println("Using HTTP proxy host       : " + httpProxyHost)
    System.out.println("Using HTTP proxy port       : " + httpProxyPort)
    System.out.println("Using HTTP non proxy hosts  : " + httpBypass)
    System.out.println("Using HTTPS proxy host       : " + httpsProxyHost)
    System.out.println("Using HTTPS proxy port       : " + httpsProxyPort)
    System.out.println("Using HTTPS non proxy hosts  : " + httpsBypass)

    if (httpProxyHost != null && !httpProxyHost.trim().isEmpty()) {
      proxyFile.parentFile.mkdirs()
      proxyFile.text = """
org.eclipse.core.net/proxyData/HTTP/host=${httpProxyHost}
org.eclipse.core.net/proxyData/HTTPS/host=${httpsProxyHost}
org.eclipse.core.net/proxyData/HTTPS/hasAuth=false
org.eclipse.core.net/proxyData/HTTP/port=${httpProxyPort}
org.eclipse.core.net/proxyData/HTTPS/port=${httpsProxyPort}
org.eclipse.core.net/org.eclipse.core.net.hasMigrated=true
org.eclipse.core.net/nonProxiedHosts=${httpBypass}
org.eclipse.core.net/systemProxiesEnabled=false
org.eclipse.core.net/proxyData/HTTP/hasAuth=false
"""
      return true
    } else
      return false

  }

  public Targetplatform getTargetplatformModel() {

    if (this.targetplatformModel == null) {
      project.logger.info("create targetplatform model for project " + project.name + "in eclipse object " + System.identityHashCode(this))
      OomphIdeExtension oomphIdeExtension = project.extensions.findByName('oomphIde')
      if (oomphIdeExtension == null)
        throw new IllegalStateException("Please use the ide extension to define your targetplatform with goomph in project " + project.name + ")")
      else {
        //Configure proxy if necessary or remove the file if not
        File proxyFile = project.file('build/egripse/proxy.ini')
        boolean proxyConfigured = createProxyFile(proxyFile)
        if (proxyConfigured) {
          oomphIdeExtension.p2director {
            addArg('plugincustomization', proxyFile.absolutePath)
          }
        }
        OomphIdeAccessor oomphIdeAccessor = new OomphIdeAccessor()
        oomphIdeAccessor.ideSetupP2(oomphIdeExtension)
        this.targetplatformModel = new Targetplatform(project)
      }
    }
    else
      project.logger.info("use targetplatform model for project " + project.name + " in eclipse object " + System.identityHashCode(this))

    return targetplatformModel

  }

  public void log() {
    for (MetaInf nextMetaInf : dependenciesCache.keySet()) {
      Collection<Dependency> deps = dependenciesCache.get(nextMetaInf)

      project.logger.info("Cache of metainf " + nextMetaInf.bundleID + "(" + System.identityHashCode(nextMetaInf) + ":")
      logDependencies("", deps)
    }
  }

  public void logDependencies(final String pre, final Collection<Dependency> deps) {
    for (Dependency nextDep : deps) {
      String resolvedString = nextDep.resolved ? nextDep.resolvedPlugin.originPath.name : "not resolved"
      project.logger.info(pre + "  - " + nextDep.bundleID + ", " + resolvedString + ", " + System.identityHashCode(nextDep))
      EclipsePlugin depPlugin = nextDep.resolvedPlugin
      if (depPlugin != null)
        logDependencies(pre + "  ", depPlugin.dependencies)
    }
  }

  public Project getProject () {
    return project
  }




}
