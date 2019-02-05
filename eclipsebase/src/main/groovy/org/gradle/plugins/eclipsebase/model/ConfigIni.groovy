package org.gradle.plugins.eclipsebase.model

public class ConfigIni {

  private List <BundlesInfoEntry> bundlesInfoEntries = new ArrayList<BundlesInfoEntry>()

  public ConfigIni (final File file) {
    File goomphPlugins = new File (System.getProperty("user.home"), '.goomph/shared-bundles/plugins')

    Properties properties = new Properties()
    properties.load(new FileReader(file))
    String bundles = properties.get("osgi.bundles")
    String [] allBundles = bundles.split(",")
    for (String nextBundles: allBundles) {
      //reference\:file\:com.google.gson_2.8.2.v20180104-1110.jar@4
      nextBundles = nextBundles.replace("\\", "").replace('reference:', '').replace('file:', '')
      String [] splitted = nextBundles.split("@")
      String identifier = splitted[0]
      String level = splitted[1]
      String ref = new File (goomphPlugins, identifier).absolutePath
      String [] tokens = identifier.replace('.jar', '')split("_")
      String bundleId = tokens[0]
      String version = tokens[1]
      BundlesInfoEntry bundlesInfoEntry = new BundlesInfoEntry()
      bundlesInfoEntry.version = version
      bundlesInfoEntry.bundleID = bundleId
      bundlesInfoEntry.ref = ref
      bundlesInfoEntry.level = level
      bundlesInfoEntries.add(bundlesInfoEntry)
    }
  }

  public List<BundlesInfoEntry> getBundlesInfoEntries () {
    return bundlesInfoEntries
  }

}
