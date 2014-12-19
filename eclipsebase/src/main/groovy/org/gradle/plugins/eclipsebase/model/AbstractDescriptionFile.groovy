package org.gradle.plugins.eclipsebase.model

import groovy.util.logging.Slf4j

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 07.06.13
 * Time: 13:14
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class AbstractDescriptionFile {

    private static final String NEWLINE = System.getProperty("line.separator")

    protected HashMap<String, Collection<String>> chapters = new HashMap<String, Collection<String>>()

    protected File file

    /**
     * reads chapter of file
     * @param inputStream      file
     * @param divider   divider to split chapter from content
     */
    public void readChapters (InputStream inputStream, final String divider) {
        Collection<String> lines = inputStream.text.split(NEWLINE)
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
