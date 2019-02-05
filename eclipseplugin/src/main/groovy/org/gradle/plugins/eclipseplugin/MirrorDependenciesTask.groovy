package org.gradle.plugins.eclipseplugin

import groovy.util.logging.Slf4j
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 27.06.13
 * Time: 16:25
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
class MirrorDependenciesTask extends DefaultTask {


    @TaskAction
    public void mirror() {

        File toPath = project.file("build/deps")

        for (File next : project.configurations.compile) {

            if (! next.name.endsWith(".jar"))
                continue

            log.info("Mirror file " + next.name + " to " + toPath.absolutePath)

            String originName = next.name
            String strippedName = originName.indexOf("_") >= 0 ? next.name.substring(0, originName.indexOf("_")) + ".jar" : next.name
            log.info ("... stripped name: " + strippedName + ")")

            project.copy {
                from next
                rename originName, strippedName
                into toPath
            }
        }

    }

}
