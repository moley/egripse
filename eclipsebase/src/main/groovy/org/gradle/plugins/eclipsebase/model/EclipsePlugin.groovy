package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j
import org.gradle.api.GradleException

import java.util.jar.JarFile
import java.util.zip.ZipEntry

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 15:14
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class EclipsePlugin extends EclipseProjectPart {

  MetaInf metainf
  File originPath

  boolean fromWorkspace

  Collection<File> bundleClasspath = new ArrayList<File>()


  public EclipsePlugin(JarFile jarfile, final File originPath) {
    super(null)
    this.originPath = originPath

    ZipEntry manifestEntry = jarfile.getEntry("META-INF/MANIFEST.MF")
    if (manifestEntry != null) {
      metainf = new MetaInf(originPath, jarfile.getInputStream(manifestEntry))
    }

    log.debug("Reading plugin from jarfile " + originPath.absolutePath + "(MetaInf " + System.identityHashCode(metainf) + ")")


    jarfile.close()

  }

  public String getBundleID() {
    return metainf != null ? metainf.bundleID : null
  }

  public Collection<Dependency> getDependencies() {
    return metainf != null ? metainf.dependencies : new ArrayList<Dependency>()
  }

  public String getFragmentHost() {
    return metainf != null ? metainf.fragmentHost : null
  }


  public EclipsePlugin(File path) {
    super(path)

    try {
      this.originPath = path

      File manifest = new File(path, "META-INF")
      File metainfFile = new File(manifest, "MANIFEST.MF")
      if (metainfFile.exists()) {

        metainf = new MetaInf(metainfFile, new FileInputStream(metainfFile))

        log.debug("Reading plugin from path " + originPath.absolutePath + "(MetaInf " + System.identityHashCode(metainf) + ")")


        //TODO make it globally available
        for (String next : metainf.bundleClasspath) {
          File nextEntry = new File(path, next)
          if (!nextEntry.exists()) {
            log.warn("BundleclasspathEntry " + nextEntry.absolutePath + " in manifest " + manifest.absolutePath + " doesnt exist")
          } else
            bundleClasspath.add(nextEntry)
        }
      }


    } catch (Exception e) {
      throw new GradleException("Error reading project " + path.absolutePath, e)
    }
  }

  @Override
  String getVersion() {
    return metainf != null ? metainf.version : null
  }

  public String toString() {
    return originPath.name
  }

  public boolean isTestPlugin() {
    return originPath.name.contains("test") //TODO make dependable from dsl
  }

  public boolean equals(Object object) {
    if (object == null)
      return false
    if (!(object instanceof EclipsePlugin))
      return false

    EclipsePlugin compPlugin = object

    return bundleID.equals(compPlugin.bundleID) //TODO version
  }
}
