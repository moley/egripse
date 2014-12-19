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
public class BuildProperties {

    Collection <String> binIncludes = new ArrayList<String>()

    protected HashMap<String, Collection<String>> chapters = new HashMap<String, Collection<String>>()

    protected File file
    /**
     * constructor
     * @param buildpropertiesFile   file
     */
    public BuildProperties (final File buildpropertiesFile) {
        this.file = buildpropertiesFile
        if (buildpropertiesFile.exists()) {
          readChapters(buildpropertiesFile.text.denormalize(), "=")
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



    /**
     * reads chapter of file
     * @param fileContent      content of file as denormalized string
     * @param divider   divider to split chapter from content
     */
    private void readChapters (String fileContent, final String divider) {
        log.info("Read content " + fileContent)
        Collection<String> lines = fileContent.split(System.lineSeparator())
        for (String nextLine: lines) {
            log.info("Reading line $nextLine from $file.absolutePath")
        }

        String currentChapter = null
        for (String next: lines) {

            if (next.trim().startsWith("#") || next.trim().isEmpty())
                continue

            String content = next
            int indexSeparator = next.indexOf(divider)
            log.info("Reading line " + next + " with divider on position " + indexSeparator)
            if (indexSeparator > 0 && ! new Character(next.charAt(0)).isWhitespace()) {
                currentChapter = next.substring(0, indexSeparator).trim()
                log.info("set current chapter to " + currentChapter)
                chapters.put(currentChapter, new ArrayList<String>())
                content = next.substring(indexSeparator + 1, next.length())
                log.info("set content to " + content)
            }

            content = content.replace(",", "").replace("\\", "").trim()
            log.info("set content to " + content)

            Collection <String> contentOfChapter = chapters.get(currentChapter)
            contentOfChapter.add(content)
            log.info("add content " + content + " to chapter " + currentChapter)
        }
    }





}
