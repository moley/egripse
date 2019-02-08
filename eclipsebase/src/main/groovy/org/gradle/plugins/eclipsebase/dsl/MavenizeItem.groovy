package org.gradle.plugins.eclipsebase.dsl


class MavenizeItem {
  String name
  String group
  String version
  String origin
  List<String> excludes = new ArrayList<String>()
}
