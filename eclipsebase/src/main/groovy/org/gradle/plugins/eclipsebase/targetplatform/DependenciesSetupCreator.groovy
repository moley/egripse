package org.gradle.plugins.eclipsebase.targetplatform

import groovy.util.logging.Slf4j
import org.gradle.plugins.eclipsebase.dsl.SetupDsl
import org.gradle.plugins.eclipsebase.model.Dependency

/**
 * Created by OleyMa on 11.12.14.
 */
@Slf4j
class DependenciesSetupCreator implements ISetupCreator {


    Set<Dependency> deps
    SetupDsl setupDsl

    public DependenciesSetupCreator (final Set<Dependency> deps, final SetupDsl setupDsl) {
        this.deps = deps
        this.setupDsl = setupDsl

    }

    @Override
    void create(File setupFile) {

        String markup = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<setup:User\n" +
                "    xmi:version=\"2.0\"\n" +
                "    xmlns:xmi=\"http://www.omg.org/XMI\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xmlns:setup=\"http://www.eclipse.org/oomph/setup/1.0\"\n" +
                "    xmlns:setup.p2=\"http://www.eclipse.org/oomph/setup/p2/1.0\"\n" +
                "    name=\"Test\"\n" +
                "    questionnaireDate=\"2014-12-10T08:53:07.827+0100\">\n" +
                "  <setupTask xsi:type=\"setup.p2:P2Task\">\n"

        for (String nextRemoteHost: setupDsl.updatesites) {
            markup += "    <repository url=\"http://andrei.gmxhome.de/eclipse/\"/>\n"
        }

        for (Dependency nextRemoteHost: deps) {
            String nextDep = "    <requirement name=\"" + nextRemoteHost.bundleID +"\"/>\n"
            if (! markup.contains(nextDep))
              markup += nextDep

        }

        markup += "  </setupTask>\n" +
                  "</setup:User>"

        log.info("Saving setup description to " + setupFile.absolutePath)
        setupFile.text = markup
    }


}
