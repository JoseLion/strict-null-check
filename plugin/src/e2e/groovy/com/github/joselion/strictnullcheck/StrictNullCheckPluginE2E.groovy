package com.github.joselion.strictnullcheck

import spock.lang.Specification

import org.gradle.testkit.runner.GradleRunner

class StrictNullCheckPluginE2E extends Specification {

  def "can apply plugin"() {
    given:
      def projectDir = new File("build/e2e")
      projectDir.mkdirs()
      new File(projectDir, "settings.gradle") << ""
      def buildGradle = new File(projectDir, "build.gradle")
      buildGradle.bytes = []
      buildGradle << """\
        |plugins {
        |  id('java')
        |  id('com.github.joselion.strict-null-check')
        |}

        |repositories {
        |  jcenter()
        |}
      |"""
      .stripMargin()

      when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("classes")
        runner.withProjectDir(projectDir)
        def result = runner.build()

      then:
        result.output.contains("BUILD SUCCESSFUL")
  }

  def "can configure the plugin extension"() {
    given:
      def projectDir = new File("build/e2e")
      projectDir.mkdirs()
      new File(projectDir, "settings.gradle") << ""
      def buildGradle = new File(projectDir, "build.gradle")
      buildGradle.bytes = []
      buildGradle << """\
        |plugins {
        |  id('java')
        |  id('com.github.joselion.strict-null-check')
        |}

        |repositories {
        |  jcenter()
        |}

        |strictNullCheck {
        |  annotations = [
        |    'my.custom.annotation.NullApi',
        |    'my.custom.annotation.NullFields'
        |  ]
        |}

        |task showAnnotations() {
        |  println('ANNOTATIONS: ' + strictNullCheck.annotations)
        |}
      |"""
      .stripMargin()

      when:
        def runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("showAnnotations")
        runner.withProjectDir(projectDir)
        def result = runner.build()

      then:
        result.output.contains("ANNOTATIONS: [my.custom.annotation.NullApi, my.custom.annotation.NullFields]")
  }
}
