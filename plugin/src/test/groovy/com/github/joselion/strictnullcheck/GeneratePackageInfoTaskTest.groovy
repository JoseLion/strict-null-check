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
        |/**
        | * This package is checked for {@code null} by the following annotations:
        | * - org.springframework.lang.NonNullApi
        | * - org.springframework.lang.NonNullFields
        | */
        |@NonNullApi
        |@NonNullFields
        |package com.github.joselion.somepackage;
        |
        |import org.springframework.lang.NonNullApi;
        |import org.springframework.lang.NonNullFields;
      |"""
      .stripMargin()
  }

  def 'getPackageJavadoc without extension.packageJavadoc'() {
    given:
      def project = ProjectBuilder.builder().build()
      def extension = new StrictNullCheckExtension(project)
      def task = project.tasks.create(
        'generatePackageInfo',
        GeneratePackageInfoTask,
        extension
      )

    when:
      def packageJavadoc = task.getPackageJavadoc()

    then:
      packageJavadoc == """\
        |/**
        | * This package is checked for {@code null} by the following annotations:
        | * - org.eclipse.jdt.annotation.NonNullByDefault
        | */"""
      .stripMargin()
  }

  def 'getPackageJavadoc with extension.packageJavadoc'() {
    given:
      def project = ProjectBuilder.builder().build()
      def extension = new StrictNullCheckExtension(project)
      extension.packageJavadoc = """\
        |@author JoseLion
        |@since v1.1.0"""
      .stripMargin()
      def task = project.tasks.create(
        'generatePackageInfo',
        GeneratePackageInfoTask,
        extension
      )

    when:
      def packageJavadoc = task.getPackageJavadoc()

    then:
      packageJavadoc == """\
        |/**
        | * This package is checked for {@code null} by the following annotations:
        | * - org.eclipse.jdt.annotation.NonNullByDefault
        | * 
        | * @author JoseLion
        | * @since v1.1.0
        | */"""
      .stripMargin()
  }
}
