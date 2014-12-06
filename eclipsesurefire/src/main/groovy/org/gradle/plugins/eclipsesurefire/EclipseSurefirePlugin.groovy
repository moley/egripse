package org.gradle.plugins.eclipsesurefire

import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by OleyMa on 21.11.14.
 */
@Slf4j
class EclipseSurefirePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        log.info("Applying plugin ${getClass()} in project ${project.name}")

        UITestDsl plugindsl = project.extensions.create("uitest", UITestDsl, project)


        project.task ('uitest', type: StartUiTestsTask, dependsOn: 'jar') {

        }
    }
}
