package com.github.joselion.strictnullcheck

import spock.lang.Specification

import org.gradle.testfixtures.ProjectBuilder

public class GeneratePackageInfoTaskTest extends Specification {

  def 'getPackageInfoTemplate test'() {
    given:
      def annotations = [
        'org.springframework.lang.NonNullApi',
        'org.springframework.lang.NonNullFields'
      ]
      def task = ProjectBuilder.builder().build().tasks.create(
        'generatePackageInfo',
        GeneratePackageInfoTask,
        'generated/',
        annotations
      )

    when:
      def template = task.getPackageInfoTemplate('com.github.joselion.somepackage')

    then:
      template == """\
        |@NonNullApi
        |@NonNullFields
        |package com.github.joselion.somepackage;
        |
        |import org.springframework.lang.NonNullApi;
        |import org.springframework.lang.NonNullFields;
      |"""
      .stripMargin()
  }
}
