package org.gradle.plugins.eclipsebase.model

import org.junit.Assert
import org.junit.Test


class ConfigIniTest {

  @Test
  public void read () {
    File goomphPlugins = new File (System.getProperty("user.home"), '.goomph/shared-bundles/plugins')
    File pluginJar = new File (goomphPlugins, 'com.google.gson_2.8.2.v20180104-1110.jar')
    ConfigIni configIni = new ConfigIni(new File ("src/test/resources/config.ini"))
    Assert.assertEquals ("BundleId 0 invalid", "com.google.gson", configIni.bundlesInfoEntries.get(0).bundleID)
    Assert.assertEquals ("Version 0 invalid", "2.8.2.v20180104-1110", configIni.bundlesInfoEntries.get(0).version)
    Assert.assertEquals ("Ref 0 invalid", pluginJar.absolutePath, configIni.bundlesInfoEntries.get(0).ref)
    Assert.assertEquals ("Level 0 invalid", "4", configIni.bundlesInfoEntries.get(0).level)
  }
}
