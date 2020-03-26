package org.gradle.plugins.eclipsebase.model


import java.nio.charset.Charset
import java.nio.file.Files

/**
 * reads the file build/oomph-ide.app/Contents/Eclipse/configuration/org.eclipse.equinox.org.eclipse.equinox.org.eclipse.equinox.simpleconfigurator/bundles.info
 * to have info about the currently installed bundles
 */
class BundlesInfo {

  private List <BundlesInfoEntry> bundlesInfoEntries = new ArrayList<BundlesInfoEntry>()

  public BundlesInfo (final File file) {
    Collection<String> lines = Files.readAllLines(file, Charset.defaultCharset())
    for (String next: lines) {
      if (! next.startsWith("#")) {
        String [] tokens = next.split(",")
        BundlesInfoEntry bundlesInfoEntry = new BundlesInfoEntry()
        bundlesInfoEntry.bundleID = tokens [0]
        bundlesInfoEntry.version = tokens [1]
        bundlesInfoEntry.ref = tokens [2]
        bundlesInfoEntry.level = tokens[3]
        bundlesInfoEntries.add(bundlesInfoEntry)
      }
    }
  }

  public List<BundlesInfoEntry> getEntries () {
    return bundlesInfoEntries
  }
}
