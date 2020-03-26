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
class ProjectVersionConfigurator {

    private String EXTENSIONKEY_QUALIFIER_TIMESTAMP = 'qualifierTimestamp'

    public void setVersion (final Project project, final String version) {

        String currentDateAsString = null
        if (project.rootProject.extensions.extraProperties.has(EXTENSIONKEY_QUALIFIER_TIMESTAMP)) {
            currentDateAsString = project.rootProject.extensions.extraProperties.get(EXTENSIONKEY_QUALIFIER_TIMESTAMP)
        }
        else {
            DateFormatter formatter = new DateFormatter(new SimpleDateFormat("yyyyMMddHHmm"))
            currentDateAsString = formatter.valueToString(new Date (System.currentTimeMillis()))
            project.rootProject.extensions.extraProperties.set(EXTENSIONKEY_QUALIFIER_TIMESTAMP, currentDateAsString)
        }
        //set version from metainf / feature.xml

        project.version = version.replace("qualifier", "v" + currentDateAsString)
        project.logger.info ("Set current version " + project.version + " in project " + project)
    }
}
