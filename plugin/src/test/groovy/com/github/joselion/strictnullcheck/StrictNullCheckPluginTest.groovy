package com.github.joselion.strictnullcheck

import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class StrictNullCheckPluginTest extends Specification {
  def 'plugin extension has default values'() {
    given:
      def project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('java')
      project.plugins.apply('com.github.joselion.strict-null-check')

    then:
      def ext = project.extensions.findByName('strictNullCheck')
      ext != null
      ext.annotations.get() == ['org.eclipse.jdt.annotation.NonNullByDefault']
      ext.generatedDir.get() == "$project.buildDir/generated/sources/strictNullCheck"
      ext.packageJavadoc.get() == ''
      ext.versions.findBugs.get() == '3.0.2'
      ext.versions.eclipseAnnotations.get() == '2.2.600'
  }


  def 'generatePackageInfo task is registered'() {
    given:
      def project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('java')
      project.plugins.apply('com.github.joselion.strict-null-check')

    then:
      project.tasks.findByName('generatePackageInfo') != null
  }

  def 'the compileJava task depends on generatePackageInfo task'() {
    given:
      def project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('java')
      project.plugins.apply('com.github.joselion.strict-null-check')

    then:
      def generateTask = project.tasks.findByName('generatePackageInfo')
      def compileJavaTask = project.tasks.getByName('compileJava')

      generateTask != null
      compileJavaTask != null
      compileJavaTask.getDependsOn().contains(generateTask) == true
  }
}
