package org.gradle.plugins.eclipseplugin.model

import org.gradle.plugins.eclipsebase.model.Dependency
import org.gradle.plugins.eclipsebase.model.MetaInf
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 14:31
 * To change this template use File | Settings | File Templates.
 */
class MetaInfTest {

    File failingFile = new File ("tmp/MANIFEST.MF")

    @Before
    public void before () {
        if (failingFile.exists())
            failingFile.delete()
        if (!failingFile.parentFile.exists())
          Assert.assertTrue (failingFile.parentFile.mkdirs())
    }



    @Test(expected = IllegalStateException)
    public void failingFile () {

        URL url = getClass().classLoader.getResource("MANIFESTError.MF")
        File file = new File (url.path).absoluteFile

        MetaInf metaInf = new MetaInf(file, new FileInputStream(file))


        println ("File $file loaded")
        println (metaInf.toString())

        metaInf.saveTo(failingFile)

        println ("File $failingFile saved")

        String content = failingFile.text
        println ("Content: " + content)

        Assert.assertFalse(content.trim().isEmpty())

    }

    @Test
    public void bundleClasspath () {
         URL url = getClass().classLoader.getResource("MANIFESTSimple.MF")
         File file = new File (url.path).absoluteFile

         MetaInf metaInf = new MetaInf(file, new FileInputStream(file))


         println ("File $file loaded")
         println (metaInf.toString())

        Collection<String> bundleClasspath = metaInf.bundleClasspath
        Assert.assertEquals ("Number of bundleclasspath entries not correct", 3, bundleClasspath.size())

        Assert.assertTrue ("castor.jar not read", bundleClasspath.contains("lib/castor/lib/castor.jar"))
        Assert.assertTrue ("xercesImpl.jar not read", bundleClasspath.contains("lib/xerces/lib/xercesImpl.jar"))
        Assert.assertTrue ("xml-apis.jar not read", bundleClasspath.contains("lib/xerces/lib/xml-apis.jar"))

    }

    @Test
    public void shortDeps () {
        URL url = getClass().classLoader.getResource("MANIFESTLast.MF")
        File file = new File (url.path).absoluteFile

        MetaInf metaInf = new MetaInf(file, new FileInputStream(file))

        metaInf.dependencies.each {println it}

        Assert.assertNotNull (metaInf.findDependency("org.eclipse.osgi"))
    }

    @Test
    public void simple () {
        URL url = getClass().classLoader.getResource("MANIFESTSimple.MF")
        File file = new File (url.path).absoluteFile

        MetaInf metaInf = new MetaInf(file, new FileInputStream(file))
        Assert.assertEquals ("1.0.0.qualifier", metaInf.version)
        Assert.assertNotNull (metaInf.findDependency("org.eclipse.gmf.runtime.diagram.ui.render"))
    }

    @Test
    public void versions () {
        URL url = getClass().classLoader.getResource("MANIFESTVersions.MF")
        File file = new File (url.path).absoluteFile

        MetaInf metaInf = new MetaInf(file, new FileInputStream(file))
        metaInf.dependencies.each {println it}

    }

    @Test
    public void multiVersions () {
        URL url = getClass().classLoader.getResource("MANIFESTMulti.MF")
        File file = new File (url.path).absoluteFile

        MetaInf metaInf = new MetaInf(file, new FileInputStream(file))
        println (metaInf.dependencies)
        Assert.assertEquals (7, metaInf.dependencies.size())

        Dependency stdlib = metaInf.findDependency("org.eclipse.xtend.util.stdlib")
        Assert.assertFalse (stdlib.reexported)

        Dependency xsd = metaInf.findDependency("org.eclipse.xsd")
        Assert.assertTrue (xsd.reexported)

    }
}
