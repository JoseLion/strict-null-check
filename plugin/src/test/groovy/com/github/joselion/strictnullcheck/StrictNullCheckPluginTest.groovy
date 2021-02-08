package com.github.joselion.strictnullcheck

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project
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
      ext.annotations == ['com.github.joselion.strictnullcheck.StrictNullPackage']
      ext.generatedDir == "$project.buildDir/generated"
      ext.findbugsVersion == '3.0.2'
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

  def 'project sourceSet is extended'() {
    given:
      def project = ProjectBuilder.builder().build()

    when:
      project.plugins.apply('java')
      project.plugins.apply("com.github.joselion.strict-null-check")

    then:
      def srcDirs = project.sourceSets.main.java.srcDirs.collect({ it.toString() })
      def generatedDir = "$project.buildDir/generated".toString()
      def javaDir = "$project.rootDir/plugin/src/main/java".toString()
      
      srcDirs.contains(generatedDir) == true
      srcDirs.contains(javaDir) == true
  }
}
