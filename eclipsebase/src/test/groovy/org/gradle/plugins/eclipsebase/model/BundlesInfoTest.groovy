package org.gradle.plugins.eclipsebase.model

import org.junit.Assert
import org.junit.Test


class BundlesInfoTest {

  @Test
  public void read () {
    BundlesInfo bundlesInfo = new BundlesInfo(new File ("src/test/resources/org.eclipse.equinox.simpleconfigurator.info"))
    Assert.assertEquals ("BundleId 0 invalid", "com.google.gson", bundlesInfo.entries.get(0).bundleID)
    Assert.assertEquals ("Version 0 invalid", "2.8.2.v20180104-1110", bundlesInfo.entries.get(0).version)
    Assert.assertEquals ("Ref 0 invalid", "../../../../../../../../../.goomph/shared-bundles/plugins/com.google.gson_2.8.2.v20180104-1110.jar", bundlesInfo.entries.get(0).ref)
    Assert.assertEquals ("Level 0 invalid", "4", bundlesInfo.entries.get(0).level)

    Assert.assertEquals ("BundleId 1 invalid", "org.apache.ant", bundlesInfo.entries.get(1).bundleID)
    Assert.assertEquals ("Version 1 invalid", "1.10.5.v20180808-0324", bundlesInfo.entries.get(1).version)
    Assert.assertEquals ("Ref 1 invalid", "../../../../../../../../../.goomph/shared-bundles/plugins/org.apache.ant_1.10.5.v20180808-0324/", bundlesInfo.entries.get(1).ref)
    Assert.assertEquals ("Level 1 invalid", "4", bundlesInfo.entries.get(1).level)
  }
}
