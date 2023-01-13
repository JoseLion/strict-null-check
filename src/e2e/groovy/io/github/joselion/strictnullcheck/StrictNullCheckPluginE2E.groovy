package io.github.joselion.strictnullcheck

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification

class StrictNullCheckPluginE2E extends Specification {

  def 'can apply plugin'() {
    given:
      def projectDir = new File('build/e2e')
      projectDir.mkdirs()
      new File(projectDir, 'settings.gradle') << ''
      def buildGradle = new File(projectDir, 'build.gradle')
      buildGradle.bytes = []
      buildGradle << '''\
        |plugins {
        |  id 'java'
        |  id 'io.github.joselion.strict-null-check'
        |}
        |
        |repositories {
        |  mavenCentral()
        |}
      |'''
      .stripMargin()
      new File('build/e2e/src/main/java/com/example/app').mkdirs()
      new File('build/e2e/src/test/java/com/example/app').mkdirs()
      def mainJava = new File('build/e2e/src/main/java/com/example/app/MainApp.java')
      mainJava.bytes = []
      mainJava << '''\
        |package com.example.app;
        |
        |public class MainApp {
        |
        |  public static void main(String[] args) {
        |  }
        |}
      |'''
      .stripMargin()

    when:
      def runner = GradleRunner.create()
      runner.forwardOutput()
      runner.withPluginClasspath()
      runner.withArguments('classes')
      runner.withProjectDir(projectDir)
      def result = runner.build()

    then:
      result.output.contains('BUILD SUCCESSFUL')
      new File('build/e2e/build/generated/sources/strictNullCheck/java/main/com/example/app/package-info.java').getText() >> '''\
        |/**
        |* This package is checked for {@code null} by the following annotations:
        |* <ul>
        |*   <li>org.eclipse.jdt.annotation.NonNullByDefault</li>
        |* </ul>
        |*/
        |@NonNullByDefault1
        |package com.example.app;
        |
        |import org.eclipse.jdt.annotation.NonNullByDefault;
      |'''
      .stripMargin()

  }

  def 'can configure the plugin extension'() {
    given:
      def projectDir = new File('build/e2e')
      projectDir.mkdirs()
      new File(projectDir, 'settings.gradle') << ''
      def buildGradle = new File(projectDir, 'build.gradle')
      buildGradle.bytes = []
      buildGradle << '''\
        |plugins {
        |  id 'java'
        |  id 'io.github.joselion.strict-null-check'
        |}
        |
        |repositories {
        |  mavenCentral()
        |}
        |
        |strictNullCheck {
        |  annotations = [
        |    'my.custom.annotation.NullApi',
        |    'my.custom.annotation.NullFields'
        |  ]
        |  versions.findBugs = '1.0.0';
        |}
        |
        |task showExtension() {
        |  println('ANNOTATIONS: ' + strictNullCheck.annotations.get())
        |  println('FINDBUGS: ' + strictNullCheck.versions.findBugs.get())
        |}
      |'''
      .stripMargin()

    when:
      def runner = GradleRunner.create()
      runner.forwardOutput()
      runner.withPluginClasspath()
      runner.withArguments('showExtension')
      runner.withProjectDir(projectDir)
      def result = runner.build()

    then:
      result.output.contains('ANNOTATIONS: [my.custom.annotation.NullApi, my.custom.annotation.NullFields]')
      result.output.contains('FINDBUGS: 1.0.0')
  }

  def "can use closures to configure the plugin extension"() {
    given:
      def projectDir = new File('build/e2e')
      projectDir.mkdirs()
      new File(projectDir, 'settings.gradle') << ''
      def buildGradle = new File(projectDir, 'build.gradle')
      buildGradle.bytes = []
      buildGradle << '''\
        |plugins {
        |  id 'java'
        |  id 'io.github.joselion.strict-null-check'
        |}
        |
        |repositories {
        |  mavenCentral()
        |}
        |
        |strictNullCheck {
        |  versions {
        |    eclipseAnnotations = '1.1.000'
        |    findBugs = '1.0.0'
        |  }
        |}
        |
        |task showVersions() {
        |  def versions = [
        |    eclipse: strictNullCheck.versions.eclipseAnnotations.get(),
        |    findBugs: strictNullCheck.versions.findBugs.get()
        |  ]
        |  println('VERSIONS: ' + versions)
        |}
      |'''
      .stripMargin()

    when:
      def runner = GradleRunner.create()
      runner.forwardOutput()
      runner.withPluginClasspath()
      runner.withArguments('showVersions')
      runner.withProjectDir(projectDir)
      def result = runner.build()

    then:
      result.output.contains('VERSIONS: [eclipse:1.1.000, findBugs:1.0.0]')
  }

  def 'can call useSpring to set Spring annotations'() {
    given:
      def projectDir = new File('build/e2e')
      projectDir.mkdirs()
      new File(projectDir, 'settings.gradle') << ''
      def buildGradle = new File(projectDir, 'build.gradle')
      buildGradle.bytes = []
      buildGradle << '''\
        |plugins {
        |  id 'java'
        |  id 'io.github.joselion.strict-null-check'
        |}
        |
        |repositories {
        |  mavenCentral()
        |}
        |
        |strictNullCheck {
        |  useSpring()
        |}
        |
        |task showAnnotations() {
        |  println('ANNOTATIONS: ' + strictNullCheck.annotations.get())
        |}
      |'''
      .stripMargin()

    when:
      def runner = GradleRunner.create()
      runner.forwardOutput()
      runner.withPluginClasspath()
      runner.withArguments('showAnnotations')
      runner.withProjectDir(projectDir)
      def result = runner.build()

    then:
      result.output.contains('ANNOTATIONS: [org.springframework.lang.NonNullApi, org.springframework.lang.NonNullFields]')
  }
}
