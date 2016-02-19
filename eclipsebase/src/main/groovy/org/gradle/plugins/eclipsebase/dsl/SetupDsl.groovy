package org.gradle.plugins.eclipsebase.dsl

/**
 * Created by OleyMa on 11.12.14.
 */
class SetupDsl {

    private EclipseBaseDsl eclipseBaseDsl

    Collection<String> updatesites = new ArrayList<String>()

    String targetplatformZip


    String localEclipse

    public SetupDsl(final EclipseBaseDsl baseDsl) {
        this.eclipseBaseDsl = baseDsl
    }

    public remoteUpdatesite (final String updatesiteUrl) {
        this.updatesites.add(updatesiteUrl);
    }

    public remoteZip (final String targetplatformZip) {
        this.targetplatformZip = targetplatformZip
    }

    public localEclipse (final String localEclispePath) {
        this.localEclipse = localEclispePath
    }



}
