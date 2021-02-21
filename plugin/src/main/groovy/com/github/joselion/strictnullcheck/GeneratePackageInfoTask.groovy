package com.github.joselion.strictnullcheck

import groovy.io.FileType

import javax.inject.Inject

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

public class GeneratePackageInfoTask extends DefaultTask {

  private StrictNullCheckExtension extension

  @Inject
  public GeneratePackageInfoTask(StrictNullCheckExtension extension) {
    super();
    this.extension = extension
  }

  @TaskAction
  def void generatePackageInfo() {
    def packages = [] as Set

    new File(".").eachFileRecurse(FileType.FILES) {
      if (it.name.endsWith('.java')) {
        packages << ((it.text =~ 'package (.+);')[0][1])
      }
    }

    packages.each { buildPackageInfo(it) }
  }

  def void buildPackageInfo(package) {
    def dotToSlash = package.replaceAll('\\.', '/')
    def dir = this.project.mkdir("${extension.generatedDir}/${dotToSlash}")
    File outputFile = new File(dir.absolutePath, 'package-info.java')
    String templateOutput = getPackageInfoTemplate(package)

    outputFile.bytes = []
    outputFile << templateOutput
  }

  def String getPackageJavadoc() {
    def annotationList = extension.annotations.collect({ ' * - ' + it }).join('\n')
    def javadoc = extension.packageJavadoc != null
      ? '\n| * \n|' + extension.packageJavadoc.split('\n').collect({ ' * ' + it }).join('\n')
      : ''

    return """\
      |/**
      | * This package is checked for {@code null} by the following annotations:
      |$annotationList$javadoc
      | */"""
    .stripMargin()
  }

  def String getPackageInfoTemplate(packageName) {
    return """\
      |${this.getPackageJavadoc()}
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
