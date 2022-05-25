package com.github.joselion.strictnullcheck

import javax.inject.Inject

import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

class StrictNullCheckExtension {

  @Input
  final ListProperty<String> annotations

  @Input
  final Property<String> generatedDir

  @Input
  final Property<String> packageJavadoc

  @Nested
  final Versions versions

  @Inject
  StrictNullCheckExtension(ObjectFactory objects, ProjectLayout layout) {
    this.annotations = objects.listProperty(String)
    this.generatedDir = objects.property(String)
    this.packageJavadoc = objects.property(String)
    this.versions = objects.newInstance(Versions)

    this.annotations.convention(['org.eclipse.jdt.annotation.NonNullByDefault'])
    this.generatedDir.convention(layout.buildDirectory.get().asFile.path + '/generated/sources/strictNullCheck')
    this.packageJavadoc.convention('')
  }

  void useSpring() {
    this.annotations.convention([
      'org.springframework.lang.NonNullApi',
      'org.springframework.lang.NonNullFields'
    ])
  }

  void versions(Closure closure) {
    closure.resolveStrategy = Closure.DELEGATE_FIRST
    closure.delegate = versions
    closure()
  }

  static class Versions {

    @Input
    final Property<String> eclipseAnnotations

    @Input
    final Property<String> findBugs

    @Inject
    Versions(ObjectFactory objects) {
      this.eclipseAnnotations = objects.property(String)
      this.findBugs = objects.property(String)

      this.eclipseAnnotations.convention('2.2.600')
      this.findBugs.convention('3.0.2')
    }
  }
}
