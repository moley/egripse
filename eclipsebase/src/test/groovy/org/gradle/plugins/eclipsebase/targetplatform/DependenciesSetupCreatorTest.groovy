package org.gradle.plugins.eclipsebase.targetplatform

import groovy.util.logging.Slf4j
import org.gradle.plugins.eclipsebase.dsl.SetupDsl
import org.gradle.plugins.eclipsebase.model.Dependency
import org.junit.Test

import java.nio.file.Files

/**
 * Created by OleyMa on 11.12.14.
 */
@Slf4j
class DependenciesSetupCreatorTest {

    @Test
    public void create () {

        File tmpFile = Files.createTempFile(getClass().simpleName, "").toFile()

        Set<Dependency> deps = new HashSet<Dependency>()
        deps.add(new Dependency('AnyEditTools.feature.group'))
        deps.add(new Dependency('org.springsource.ide.eclipse.gradle.feature.group'))
        deps.add(new Dependency('org.springsource.ide.eclipse.gradle.feature.group'))


        SetupDsl setupDsl = new SetupDsl(null)
        setupDsl.remoteUpdatesite ('http://andrei.gmxhome.de/eclipse/')
        setupDsl.remoteUpdatesite ('http://dist.springsource.com/release/TOOLS/update/e4.4/')

        DependenciesSetupCreator creator = new DependenciesSetupCreator(deps, setupDsl)
        creator.create(tmpFile)

        log.info(tmpFile.text)


    }
}
