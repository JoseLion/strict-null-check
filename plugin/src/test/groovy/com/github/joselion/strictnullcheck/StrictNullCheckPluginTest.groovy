package com.github.joselion.strictnullcheck

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class StrictNullCheckPluginTest extends Specification {
  def 'plugin extension has default values'() {
    given:
      def project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('java')
      project.plugins.apply("com.github.joselion.strict-null-check")

    then:
      def ext = project.extensions.findByName('strictNullCheck')
      ext != null
      ext.annotations == ['org.eclipse.jdt.annotation.NonNullByDefault']
      ext.generatedDir == "$project.buildDir/generated"
      ext.packageJavadoc == null
      ext.versions.findBugs == '3.0.2'
      ext.versions.eclipseAnnotations == '2.2.600'
  }


  def 'generatePackageInfo task is registered'() {
    given:
      def project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('java')
      project.plugins.apply("com.github.joselion.strict-null-check")

    then:
      project.tasks.findByName("generatePackageInfo") != null
  }

  def 'the classes task is finalized by generatePackageInfo task'() {
    given:
      def project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('java')
      project.plugins.apply("com.github.joselion.strict-null-check")

    then:
      def generateTask = project.tasks.findByName("generatePackageInfo")
      def classesTask = project.tasks.getByName('classes')

      generateTask != null
      classesTask != null
      classesTask.getFinalizedBy().getDependencies().contains(generateTask) == true
  }
}
