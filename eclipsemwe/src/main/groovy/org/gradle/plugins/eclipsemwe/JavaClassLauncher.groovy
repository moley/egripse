package org.gradle.plugins.eclipsemwe

import groovy.util.logging.Slf4j
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection

import java.util.logging.Level

/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 21.06.13
 * Time: 10:58
 * To change this template use File | Settings | File Templates.
 */
@Slf4j
public class JavaClassLauncher {

        /** old classloader is saved in this variable to be reset calling method {@link #resetClassloader}.  */
        private ClassLoader oldClassloader

        /** URLclassloader which is used  */
        ClassLoader usedClassloader

        /**
         * Load the given class with a url classloader containing the given classpath.
         *
         * @param classpath classpath to use loading clazz
         * @param clazz fullqualified name of class to be used
         * @return loaded class
         */
        public def loadGenerator(final FileCollection classpath, final String clazz) {
            if (classpath == null)
                throw new IllegalStateException("loadGenerator must not be called with a classpath null")

            oldClassloader = Thread.currentThread().contextClassLoader

            def urls = classpath.collect { it.toURI().toURL() }
            if (log.isLoggable(Level.FINEST)) {
                for (URL url : urls) {
                    log.finest("URLComp of classpath : -" + url)
                    File file = new File (url.path)
                    if (! file.exists())
                        log.finest(".... file " + file.absolutePath + " does not exist physically on disk")
                }
            }

            usedClassloader = new URLClassLoader(urls as URL[])
            Thread.currentThread().contextClassLoader = usedClassloader
            return loadClassWithUrlClassloader(clazz)
        }

        /**
         * Loads the given class with the established classloader.
         *
         * @param clazz Class to load
         */
        public def loadClassWithUrlClassloader(final String clazz) {
            if (usedClassloader == null)
                throw new GradleException("URLClassloader was used, before loadGenerator() was called")
            return usedClassloader.loadClass(clazz);
        }

        /**
         * Method resets the classloader which was saved before.
         * Must not be called without previously calling {@link #loadGenerator}
         */
        public void resetClassloader() {
            if (oldClassloader != null) {
                Thread.currentThread().contextClassLoader = oldClassloader;
                oldClassloader = null
                usedClassloader = null
            } else throw new RuntimeException("Classloader was not saved, I cannot reset any classloader")

        }
}
