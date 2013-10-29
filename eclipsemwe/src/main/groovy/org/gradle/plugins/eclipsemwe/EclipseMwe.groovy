package org.gradle.plugins.eclipsemwe

import org.gradle.api.Project

import java.lang.reflect.Array

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 19.05.13
 * Time: 01:47
 * To change this template use File | Settings | File Templates.
 */
class EclipseMwe {

    Collection<String> mweFiles = new ArrayList<String>()

    private Project project

    public EclipseMwe (final Project project) {
        this.project = project
    }

    void mweFile (final String file) {
        mweFiles.add(file)
    }

}
