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

    File writtenFile = new File ("tmp/MANIFEST.MF")

    @Before
    public void before () {
        if (writtenFile.exists())
            writtenFile.delete()
        if (!writtenFile.parentFile.exists())
          Assert.assertTrue (writtenFile.parentFile.mkdirs())
    }

    /**
     * loads metainf from url
     * @param urlAsString       url to load from
     * @return metainf
     */
    MetaInf loadFromUrl (final String urlAsString) {
        URL url = getClass().classLoader.getResource(urlAsString)
        File file = new File (url.path).absoluteFile
        println ("File $file loaded")
        MetaInf metaInf = new MetaInf(file, new FileInputStream(file))
        println (metaInf.toString())
        return metaInf
    }


    @Test
    public void hostplugin () {
        MetaInf metainf = loadFromUrl('MANIFESTHostplugin.MF')
        Assert.assertEquals ('org.eclipse.swt', metainf.fragmentHost)

    }

    @Test(expected = IllegalStateException)
    public void failingWrittenFile () {
        MetaInf metaInf = loadFromUrl('MANIFESTError.MF')

        metaInf.saveTo(writtenFile)

        println ("File $writtenFile saved")

        String content = writtenFile.text
        println ('Content: ' + content)

        Assert.assertFalse(content.trim().isEmpty())

    }

    @Test
    public void bundleClasspath () {
         MetaInf metaInf = loadFromUrl('MANIFESTSimple.MF')

        Collection<String> bundleClasspath = metaInf.bundleClasspath
        Assert.assertEquals ('Number of bundleclasspath entries not correct', 3, bundleClasspath.size())

        Assert.assertTrue ('castor.jar not read', bundleClasspath.contains('lib/castor/lib/castor.jar'))
        Assert.assertTrue ('xercesImpl.jar not read', bundleClasspath.contains('lib/xerces/lib/xercesImpl.jar'))
        Assert.assertTrue ('xml-apis.jar not read', bundleClasspath.contains('lib/xerces/lib/xml-apis.jar'))

        metaInf.saveTo(writtenFile)

    }

    @Test
    public void shortDeps () {
        MetaInf metaInf = loadFromUrl('MANIFESTLast.MF')
        metaInf.dependencies.each {println it}
        Assert.assertNotNull (metaInf.findDependency('org.eclipse.osgi'))
    }

    @Test
    public void simple () {
        MetaInf metaInf = loadFromUrl('MANIFESTSimple.MF')
        Assert.assertEquals ('1.0.0.qualifier', metaInf.version)
        Assert.assertNotNull (metaInf.findDependency('org.eclipse.gmf.runtime.diagram.ui.render'))
    }

    @Test
    public void versions () {
        MetaInf metaInf = loadFromUrl('MANIFESTVersions.MF')
        metaInf.dependencies.each {println it}

    }

    @Test
    public void multiVersions () {
        MetaInf metaInf = loadFromUrl('MANIFESTMulti.MF')
        println (metaInf.dependencies)
        Assert.assertEquals (7, metaInf.dependencies.size())

        Dependency stdlib = metaInf.findDependency('org.eclipse.xtend.util.stdlib')
        Assert.assertFalse (stdlib.reexported)

        Dependency xsd = metaInf.findDependency('org.eclipse.xsd')
        Assert.assertTrue (xsd.reexported)

    }
}
