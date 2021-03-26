package com.github.joselion.strictnullcheck

import groovy.io.FileType

import java.util.Set

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

public class GeneratePackageInfoTask extends DefaultTask {

  @Input
  private StrictNullCheckExtension extension

  @Inject
  public GeneratePackageInfoTask(StrictNullCheckExtension extension) {
    super();
    this.extension = extension
  }

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
    return new File(this.extension.generatedDir)
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
    def annotationList = extension.annotations.collect({ " *   <li>$it</li>" }).join('\n')
    def javadoc = extension.packageJavadoc != null
      ? '\n| * \n|' + extension.packageJavadoc.split('\n').collect({ " * $it" }).join('\n')
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
      |${extension.annotations.collect({ cannonicalToAnnotation(it) }).join('\n')}
      |package $packageName;

      |${extension.annotations.collect({ cannonicalToImport(it) }).join('\n')}
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
