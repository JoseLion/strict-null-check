package com.github.joselion.strictnullcheck

import java.util.List

import org.gradle.api.Project

public class StrictNullCheckExtension {

  private Project project

  List<String> annotations = List.of('com.github.joselion.strictnullcheck.StrictNullPackage')

  String generatedDir = "$project.buildDir/generated".toString()

  String findbugsVersion = '3.0.2'

  public StrictNullCheckExtension(Project project) {
    this.project = project
  }
}
