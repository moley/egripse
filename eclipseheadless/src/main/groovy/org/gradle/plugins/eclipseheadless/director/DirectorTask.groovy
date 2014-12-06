package org.gradle.plugins.eclipseheadless.director

import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.eclipseheadless.HeadlessApplicationTask
import org.gradle.process.ExecResult

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 04.12.13
 * Time: 23:56
 * To change this template use File | Settings | File Templates.
 */
public class DirectorTask extends HeadlessApplicationTask{

    private String repository
    private String installIU


    public void repository (final String repo) {
        this.repository = repo
    }

    public void installIU (final String installIU) {
        this.installIU = installIU
    }

    @TaskAction
    ExecResult startHeadlessApplication() {
        applicationname = 'org.eclipse.equinox.p2.director'

        if (repository == null)
            throw new IllegalStateException("You have to define a repository")

        if (installIU == null)
            throw new IllegalStateException("You have to define an installIU")

        String repositoryAbsolute = "file:/" + project.file(repository).absolutePath

        parameter("-repository")
        parameter(repositoryAbsolute)
        parameter("-installIU")
        parameter(installIU)

        return super.startHeadlessApplication()


    }

}
