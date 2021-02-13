package com.github.joselion.strictnullcheck

import spock.lang.Specification

import org.gradle.testfixtures.ProjectBuilder

class GeneratePackageInfoTaskTest extends Specification {

  def 'getPackageInfoTemplate test'() {
    given:
      def project = ProjectBuilder.builder().build()
      def extension = new StrictNullCheckExtension(project)
      extension.annotations = [
        'org.springframework.lang.NonNullApi',
        'org.springframework.lang.NonNullFields'
      ]
      def task = project.tasks.create(
        'generatePackageInfo',
        GeneratePackageInfoTask,
        extension
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
