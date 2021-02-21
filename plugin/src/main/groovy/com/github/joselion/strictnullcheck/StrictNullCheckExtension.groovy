package com.github.joselion.strictnullcheck

import java.util.List

import org.gradle.api.Project

public class StrictNullCheckExtension {

  private Project project

  List<String> annotations = ['org.eclipse.jdt.annotation.NonNullByDefault']

  String generatedDir = "$project.buildDir/generated".toString()

  Versions versions = new Versions()

  public StrictNullCheckExtension(Project project) {
    this.project = project
  }

  public void useSpring() {
    this.annotations = [
      'org.springframework.lang.NonNullApi',
      'org.springframework.lang.NonNullFields'
    ]
  }

  public static class Versions {

    String eclipseAnnotations = '2.2.600'

    String findBugs = '3.0.2'
  }
}
