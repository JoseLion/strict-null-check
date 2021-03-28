package com.github.joselion.strictnullcheck

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin

class StrictNullCheckPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.extensions.create('strictNullCheck', StrictNullCheckExtension)

    project.plugins.withType(JavaPlugin) {
      Configuration configuration = project.getConfigurations().getByName('compileOnly')
      String findbugsVersion = project.strictNullCheck.versions.get().findBugs.get()
      String eclipseVersion = project.strictNullCheck.versions.get().eclipseAnnotations.get()

      configuration.getDependencies().addAll([
        project.getDependencies().create("com.google.code.findbugs:jsr305:$findbugsVersion"),
        project.getDependencies().create("org.eclipse.jdt:org.eclipse.jdt.annotation:$eclipseVersion")
      ])

      project.sourceSets.main.java.srcDirs(project.strictNullCheck.generatedDir.get())

      project.task('generatePackageInfo', type: GeneratePackageInfoTask) {
        annotations = project.strictNullCheck.annotations
        outputDir = project.strictNullCheck.generatedDir
        packageJavadoc = project.strictNullCheck.packageJavadoc
      }

      project.tasks.classes.finalizedBy(project.tasks.generatePackageInfo)
    }
  }
}
