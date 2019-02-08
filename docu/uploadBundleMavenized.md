# Uploading bundles to maven repositories

## Configure dependencies to be mavenized
If you want to use eclipse bundles with a plain maven build, you can define
some bundles to be uploaded to a maven repo. Select one bundle plugin,were you define
the bundle to be uploaded as dependency in ```META-INF/MANIFEST.MF```, which tells egripse to
add this bundle to the compile configuration of your project:
```
...
Require-Bundle: org.eclipse.core.runtime,
...
```


Afterwards add a mavenize method to the eclipseplugin closure for every bundle to be mavenized
where ``org.egripse.testproject.flat`` is the group to be uploaded, `runtime` is the new artifact name,
and `org.eclipse.core.runtime` is the origin bundlename to be used:
```
eclipseplugin {
    mavenize ('runtime', 'org.egripse.testproject.flat', 'org.eclipse.core.runtime')
}
```

Finally call `gradle publishToMavenLocal` in the subproject and take a look at your .m2 path in your home folder,
were maven stores local artifacts. Your bundle should be uploaded.

**Restriction: Currently no dependency info is added in pom.xml**

## Configure upload repo
**egripse** uses the [maven-publish plugin](https://docs.gradle.org/current/userguide/publishing_maven.html) to upload the maven artifacts.
So you have to configure an upload repository like:
```
def mavenUrl = System.getProperty("maven.url")
def mavenUser = System.getProperty("maven.user")
def mavenPwd = System.getProperty("maven.password")
publishing {
  repositories {
    maven {
      credentials {
        username mavenUser
        password mavenPwd
      }
      url mavenUrl
    }
  }
}

```

If you call `gradle publish` the artifact should be uploaded to your defined repository.


