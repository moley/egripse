package org.gradle.plugins.eclipsesurefire

import groovy.util.logging.Slf4j
import org.gradle.api.Project

/**
 * Created by OleyMa on 28.11.14.
 */
@Slf4j
public class UITestDsl {

    Project project

    boolean failIfNoTests = false

    boolean testFailureIgnore

    int debugPort = 0

    boolean showDebugInfo = false

    boolean excludeUiTestsInStandaloneTests = true

    List<String> includes = new ArrayList<>()

    List<String> excludes = new ArrayList<>()


    public UITestDsl (final Project project) {
        this.project = project
        log.info("Constructing patternsets in closure " + getClass().getName() + "(" + System.identityHashCode(this) + ")")
        include('**/uitest/**/Test*.class', '**/uitest/**/*Test.class', '**/uitest/**/*TestCase.class')
        exclude('**/Abstract*Test.class', '**/Abstract*TestCase.class', '**/*$*')

    }

    public include (String... includes) {
        this.includes.addAll(includes)
    }

    public exclude (String ... excludes) {
        this.excludes.addAll(excludes)

    }




    String getIncludesAsString () {
        for (String next: includes)
            log.info("< Next: " + next + ">")
        String joinedIncludes = includes.join(",")
        log.info("getIncludesAsString <" + joinedIncludes + ">")
        return joinedIncludes
    }

    Set<String> getIncludes () {
        log.info("includes $includes (" + System.identityHashCode(this) + ")")
        return includes
    }

    Set<String> getExcludes () {
        log.info("excludes $excludes (" + System.identityHashCode(this) + ")")
        return excludes
    }

    String getExcludesAsString () {
        String joinedExcludes = excludes.join(",")
        log.info("getExcludesAsString <" + joinedExcludes + ">")
        return joinedExcludes
    }



}
