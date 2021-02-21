package com.github.joselion.strictnullcheck

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin

class StrictNullCheckPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    def extension = project.extensions.create('strictNullCheck', StrictNullCheckExtension, project)

    project.plugins.withType(JavaPlugin) {
      Configuration configuration = project.getConfigurations().getByName('compileOnly')
      configuration.getDependencies().addAll([
        project.getDependencies().create("com.google.code.findbugs:jsr305:${extension.versions.findBugs}"),
        project.getDependencies().create("org.eclipse.jdt:org.eclipse.jdt.annotation:${extension.versions.eclipseAnnotations}")
      ])

      project.sourceSets.main.java.srcDirs(extension.generatedDir)

      project.tasks.create(
        'generatePackageInfo',
        GeneratePackageInfoTask,
        extension
      )

      project.tasks.classes.finalizedBy(project.tasks.generatePackageInfo)
    }
  }
}
