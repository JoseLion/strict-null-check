package com.github.joselion.strictnullcheck

import groovy.io.FileType
import groovy.text.SimpleTemplateEngine

import java.util.List

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin

class StrictNullCheckPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    def ext = project.extensions.create(
      'strictNullCheck',
      StrictNullCheckExtension,
      List.of('com.github.joselion.strictnullcheck.StrictNullPackage'),
      "$project.buildDir/generated".toString(),
      '3.0.2'
    )

    project.plugins.withType(JavaPlugin) {
      Configuration configuration = project.getConfigurations().getByName('compileOnly')
      configuration.getDependencies().add(
        project.getDependencies().create("com.google.code.findbugs:jsr305:$ext.findbugsVersion")
      )

      project.sourceSets.main.java.srcDirs(ext.generatedDir, 'plugin/src/main/java')

      project.tasks.create(
        'generatePackageInfo',
        GeneratePackageInfoTask,
        ext.generatedDir,
        ext.annotations
      )

      project.tasks.classes.finalizedBy(project.tasks.generatePackageInfo)
    }
  }
}
