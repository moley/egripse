package org.gradle.plugins.eclipsebase.dsl
/**
 * Created with IntelliJ IDEA.
 * User: OleyMa
 * Date: 05.07.13
 * Time: 10:21
 * To change this template use File | Settings | File Templates.
 */
class UpdatesiteDsl {


    String host
    String path
    String categoriesXml
    String user = "ftp"
    String pwd = "ftp"

    private EclipseBaseDsl baseDsl

    public UpdatesiteDsl(final EclipseBaseDsl baseDsl) {
        this.baseDsl = baseDsl
    }

}
