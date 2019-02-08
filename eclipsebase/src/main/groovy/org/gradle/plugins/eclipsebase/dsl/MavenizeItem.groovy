package org.gradle.plugins.eclipsebase.dsl


class MavenizeItem {
  String name
  String group
  String version
  String origin
  File jarFile
  List<String> excludes = new ArrayList<String>()
}
