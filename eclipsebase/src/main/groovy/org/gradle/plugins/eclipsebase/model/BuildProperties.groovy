package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 23.05.13
 * Time: 23:58
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class BuildProperties extends AbstractDescriptionFile{

    Collection <String> binIncludes = new ArrayList<String>()

    /**
     * constructor
     * @param buildpropertiesFile   file
     */
    public BuildProperties (final File buildpropertiesFile) {
        this.file = buildpropertiesFile
        if (buildpropertiesFile.exists()) {
          readChapters(new FileInputStream(buildpropertiesFile), "=")
          readBinIncludes()
        }
    }

    /**
     * reads bin includes chapter
     */
    private void readBinIncludes () {
        Collection<String> bundlestrings = chapters.get("bin.includes")

        for (String nextBundlestring: bundlestrings) {
            if (! nextBundlestring.equals("."))
              binIncludes.add(nextBundlestring)
        }
    }





}
