# Building your bundles and features


## Define a targetplatform in the root project
If you like to build your bundles and features with egripse you have to do with three plugin,
the `eclipsebase` gradle plugin has to be applied to the root project and is necessary to resolve dependencies
via goomph for all of your bundles and features.
Add the following to the root project:
```grooy
apply {
  plugin 'eclipsebase'
}

oomphIde {
  repo 'http://download.eclipse.org/releases/2018-12'
  repo 'http://download.eclipse.org/buildship/updates/e48/snapshots/3.x/'
  feature 'org.eclipse.epp.package.committers.feature'
  feature 'org.eclipse.buildship'

  jdt {}
}
```

## Apply eclipseplugin to any bundle subproject
Add the following in any sub project containing an eclipse bundle (a module, which contains a **META-INF/MANIFEST.MF** file
```groovy
apply {
    plugin 'eclipseplugin'
}
```

## Apply eclipsefeature to any feature subproject
At last add the following to any sub project containing an eclipse feature (a module, which contains a **feature.xml** file:
```groovy
apply {
    plugin 'eclipsefeature'
}
```

