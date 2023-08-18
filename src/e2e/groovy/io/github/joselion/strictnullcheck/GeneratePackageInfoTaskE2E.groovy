package io.github.joselion.strictnullcheck

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification

class GeneratePackageInfoTaskE2E extends Specification {

  def 'generates the package-info.java if it does not already exist'() {
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
      new File('build/e2e/src/main/java/com/example/app/other').mkdirs()
      new File('build/e2e/src/test/java/com/example/app/other').mkdirs()
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
      def packageInfo = new File('build/e2e/src/main/java/com/example/app/package-info.java')
      packageInfo.bytes = []
      packageInfo << '''\
        |package com.example.app;
      |'''
      .stripMargin()
      def otherJava = new File('build/e2e/src/main/java/com/example/app/other/Other.java')
      otherJava.bytes = []
      otherJava << '''\
        |package com.example.app.other;
        |
        |public class Other {
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
      new File('build/e2e/build/generated/sources/strictNullCheck/java/main/com/example/app/package-info.java').exists() == false
      new File('build/e2e/build/generated/sources/strictNullCheck/java/main/com/example/app/other/package-info.java').exists() == true
  }
}