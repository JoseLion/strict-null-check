package com.github.joselion.strictnullcheck

import groovy.io.FileType

import java.util.List
import java.util.Set

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

public class GeneratePackageInfoTask extends DefaultTask {

  @Input
  final Property<String> outputDir = project.objects.property(String)

  @Input
  final ListProperty<String> annotations = project.objects.listProperty(String)

  @Input
  final Property<String> packageJavadoc = project.objects.property(String)

  @InputFiles
  def Set<File> getSourcePackages() {
    def packages = [] as Set

    new File(".").eachFileRecurse(FileType.FILES) {
      if (it.name.endsWith('.java')) {
        packages << ((it.text =~ 'package (.+);')[0][1])
      }
    }

    return packages
  }

  @OutputDirectory
  def File getGeneratedDir() {
    return new File(this.outputDir.get())
  }

  @TaskAction
  def void generatePackageInfo() {
    getSourcePackages().each { buildPackageInfo(it) }
  }

  def void buildPackageInfo(package) {
    String basePath = getGeneratedDir().path
    String dottedPath = package.replaceAll('\\.', '/')
    File dir = this.project.mkdir("${basePath}/${dottedPath}")
    File outputFile = new File(dir.absolutePath, 'package-info.java')
    String templateOutput = getPackageInfoTemplate(package)

    outputFile.bytes = []
    outputFile << templateOutput
  }

  def String buildPackageJavadoc() {
    def annotationList = this.annotations.get().collect({ " *   <li>$it</li>" }).join('\n')
    def javadoc = !this.packageJavadoc.get().isEmpty()
      ? '\n| * \n|' + this.packageJavadoc.get().split('\n').collect({ " * $it" }).join('\n')
      : ''

    return """\
      |/**
      | * This package is checked for {@code null} by the following annotations:
      | * <ul>
      |$annotationList
      | * </ul>$javadoc
      | */"""
    .stripMargin()
  }

  def String getPackageInfoTemplate(packageName) {
    return """\
      |${buildPackageJavadoc()}
      |${this.annotations.get().collect({ cannonicalToAnnotation(it) }).join('\n')}
      |package $packageName;

      |${this.annotations.get().collect({ cannonicalToImport(it) }).join('\n')}
    |"""
    .stripMargin()
  }

  def String cannonicalToAnnotation(String cannonical) {
    String className = cannonical.split(/\./).last()
    return "@$className"
  }

  def String cannonicalToImport(String cannonical) {
    return "import $cannonical;"
  }
}
