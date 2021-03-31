package com.github.joselion.strictnullcheck

import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class GeneratePackageInfoTaskTest extends Specification {

  def 'getPackageInfoTemplate test'() {
    given:
      def project = ProjectBuilder.builder().build()
      def task = project.task('generatePackageInfo', type: GeneratePackageInfoTask) {
        annotations = [
          'org.springframework.lang.NonNullApi',
          'org.springframework.lang.NonNullFields'
        ]
        packageJavadoc = ''
        outputDir = "$project.buildDir/genereted"
      }

    when:
      def template = task.getPackageInfoTemplate('com.github.joselion.somepackage')

    then:
      template == '''\
        |/**
        | * This package is checked for {@code null} by the following annotations:
        | * <ul>
        | *   <li>org.springframework.lang.NonNullApi</li>
        | *   <li>org.springframework.lang.NonNullFields</li>
        | * </ul>
        | */
        |@NonNullApi
        |@NonNullFields
        |package com.github.joselion.somepackage;
        |
        |import org.springframework.lang.NonNullApi;
        |import org.springframework.lang.NonNullFields;
      |'''
      .stripMargin()
  }

  def 'buildPackageJavadoc without extension.packageJavadoc'() {
    given:
      def project = ProjectBuilder.builder().build()
      def task = project.task('generatePackageInfo', type: GeneratePackageInfoTask) {
        annotations = ['org.eclipse.jdt.annotation.NonNullByDefault']
        packageJavadoc = ''
        outputDir = "$project.buildDir/genereted"
      }

    when:
      def packageJavadoc = task.buildPackageJavadoc()

    then:
      packageJavadoc == '''\
        |/**
        | * This package is checked for {@code null} by the following annotations:
        | * <ul>
        | *   <li>org.eclipse.jdt.annotation.NonNullByDefault</li>
        | * </ul>
        | */'''
      .stripMargin()
  }

  def 'buildPackageJavadoc with extension.packageJavadoc'() {
    given:
      def project = ProjectBuilder.builder().build()
      def task = project.task('generatePackageInfo', type: GeneratePackageInfoTask) {
        annotations = ['org.eclipse.jdt.annotation.NonNullByDefault']
        packageJavadoc = '''\
          |@author JoseLion
          |@since v1.1.0'''
        .stripMargin()
        outputDir = "$project.buildDir/genereted"
      }

    when:
      def packageJavadoc = task.buildPackageJavadoc()

    then:
      packageJavadoc == '''\
        |/**
        | * This package is checked for {@code null} by the following annotations:
        | * <ul>
        | *   <li>org.eclipse.jdt.annotation.NonNullByDefault</li>
        | * </ul>
        | * 
        | * @author JoseLion
        | * @since v1.1.0
        | */'''
      .stripMargin()
  }
}
