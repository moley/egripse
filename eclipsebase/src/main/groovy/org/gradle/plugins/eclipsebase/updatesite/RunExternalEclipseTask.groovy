package org.gradle.plugins.eclipsebase.updatesite

import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.plugins.eclipsebase.model.Eclipse
import org.gradle.plugins.eclipsebase.model.Targetplatform

/**
 * Created by OleyMa on 19.02.16.
 */
public abstract class RunExternalEclipseTask extends JavaExec {

    public Targetplatform getExternalEclipse(Project project) {
      Eclipse eclipse = project.eclipsemodel
      String localEclipse = eclipse.eclipseDsl.setup.localEclipse
      if (localEclipse == null)
        throw new IllegalStateException("Please configure the path to an eclipse installation")
      File localPath = new File(localEclipse)
      if (!localPath.exists())
        throw new IllegalStateException("Local path " + localPath.absolutePath + " does not exist, please configure a valid path")

      return new Targetplatform(project, localPath)
    }

}
