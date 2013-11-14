package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j

import java.util.jar.Manifest

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 17.05.13
 * Time: 14:29
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class MetaInf {

    Set<Dependency> dependencies = new HashSet<Dependency>()

    String bundleID

    String version

    Collection<String> bundleClasspath = new HashSet<String> ()

    Manifest manifest

    private static final String NEWLINE = System.getProperty("line.separator")
    private final File file
    private final InputStream inputStream

    public MetaInf (final File file, final InputStream metainfFile) {
        this.file = file


        manifest = new Manifest(metainfFile)

        createRequireBundle()
        createBundleId()
        createBundleClasspath()
        createVersion()

        log.debug("Read metainf " + bundleID)
    }

    public Dependency findDependency (final String bundleID) {
        for (Dependency nextDep: dependencies) {
            if (nextDep.bundleID.equals(bundleID))
                return nextDep
        }

        return null
    }

    public void setVersion (final String version) {
        this.version = version
        log.info ("set version " + version + " in file " + file.absolutePath)
        manifest.mainAttributes.putValue("Bundle-Version", version)
    }

    public void save () {
        if (file.name.equals("MANIFEST.MF")) {
          log.info("Writing $file")
          FileOutputStream fos = new FileOutputStream(file)
          manifest.write(fos)
        }
        else
            throw new IllegalStateException("You try to write a MANIFEST.MF which is encapsualted in another file. That is not supported")
    }


    private void createRequireBundle () {
        String requireBundleString = manifest.mainAttributes.getValue("Require-Bundle")
        if (requireBundleString == null)
            return

        Collection<String> deps = new ArrayList<String>()
        String currentToken = ""
        boolean inString = false;

        for (int i = 0; i < requireBundleString.length(); i++) {
            if ((requireBundleString.charAt(i) == ',' && ! inString)) {
                deps.add(currentToken)
                currentToken = ""
                continue
            }

            if (requireBundleString.charAt(i) == '"')
                inString = ! inString

            currentToken += requireBundleString.getAt(i)
        }

        if (! currentToken.trim().isEmpty())
            deps.add(currentToken)

        for (String nextBundleString: deps)
            dependencies.add(new Dependency(nextBundleString))
    }

    private void createVersion () {
        version = manifest.mainAttributes.getValue("Bundle-Version")
    }

    private void createBundleId () {
        String bundlestrings =  manifest.mainAttributes.getValue("Bundle-SymbolicName")
        if (bundlestrings == null)
            return

        String [] tokens = bundlestrings.split(";")
        bundleID = tokens [0]
    }

    private void createBundleClasspath () {
        String classpathAsString = manifest.mainAttributes.getValue("Bundle-ClassPath")
        if (classpathAsString == null)
            return

        List<String> bundlestrings = classpathAsString.split("\n|,")
        if (bundlestrings != null) {
          for (String nextBundleString: bundlestrings) {
            if (! nextBundleString.trim().equals("."))
              bundleClasspath.add(nextBundleString)
          }
        }

    }



    public String toString () {
        String asString = ""

        asString += "Dependencies:" + NEWLINE
        for (Dependency nextDep: dependencies) {
            asString +="  - " + nextDep + NEWLINE
        }

        return asString
    }


}
