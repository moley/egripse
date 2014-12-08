package org.gradle.plugins.eclipsesurefire

import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Created by OleyMa on 21.11.14.
 */
@Slf4j
class EclipseSurefirePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        log.info("Applying plugin ${getClass()} in project ${project.name}")

        UITestDsl uiTestDsl = project.extensions.create("uitest", UITestDsl, project)

        if (uiTestDsl != null) {
            if (uiTestDsl.excludeUiTestsInStandaloneTests) {
                log.info("exclude ui tests in standalone tests: <" + uiTestDsl.includes + ">")
                uiTestDsl.includes.each {
                    project.test.exclude (it)
                }
            }
        }
        else
            log.info("No uitest closure defined")

        log.info("Excludes in test: " + project.test.excludes)


        Task uitestTask = project.task ('uitest', type: StartUiTestsTask, dependsOn: 'jar')
        uitestTask.dependsOn('jar')

    }
}
