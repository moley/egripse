package org.gradle.plugins.eclipsesurefire

import org.gradle.api.Project

/**
 * Created by OleyMa on 28.11.14.
 */
public class UITestDsl {

    Project project


    public UITestDsl (final Project project) {
        this.project = project
    }


    boolean failIfNoTests = false

    boolean testFailureIgnore

    int debugPort = 0

    boolean showDebugInfo = false
}
