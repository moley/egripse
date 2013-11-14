package org.gradle.plugins.eclipsebase.config

import groovy.util.logging.Slf4j
import org.gradle.api.Project

import javax.swing.text.DateFormatter
import java.text.SimpleDateFormat

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 03.07.13
 * Time: 08:46
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class ProjectVersionConfigurator {

    private static Date currentBuildDate

    static {
        currentBuildDate = new Date (System.currentTimeMillis())
    }

    public void setVersion (final Project project, final String version) {
        //set version from metainf / feature.xml
        DateFormatter formatter = new DateFormatter(new SimpleDateFormat("yyyyMMddHHmm"))
        String currentDateAsString = formatter.valueToString(currentBuildDate)
        project.version = version.replace("qualifier", "v" + currentDateAsString)
        log.info ("Set current version " + project.version + " in project " + project)
    }
}
