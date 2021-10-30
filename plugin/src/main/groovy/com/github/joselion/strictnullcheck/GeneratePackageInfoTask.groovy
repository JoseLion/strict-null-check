package com.github.joselion.strictnullcheck

import groovy.io.FileType

import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class GeneratePackageInfoTask extends DefaultTask {

  @Input
  final Property<String> outputDir = project.objects.property(String)

  @Input
  final ListProperty<String> annotations = project.objects.listProperty(String)

  @Input
  final Property<String> packageJavadoc = project.objects.property(String)

  @InputFiles
  Set<File> getSourcePackages() {
    Set packages = [] as Set

    new File('./src').eachFileRecurse(FileType.FILES) {
      def matcher = it.text =~ 'package (.+);'

      if (it.name.endsWith('.java') && matcher.find()) {
        packages << matcher.group(1)
      }
    }

    return packages
  }

  @OutputDirectory
  File getGeneratedDir() {
    return new File(this.outputDir.get())
  }

  @TaskAction
  void generatePackageInfo() {
    getSourcePackages().each(this.&buildPackageInfo)
  }

  void buildPackageInfo(String packageName) {
    String basePath = getGeneratedDir().path
    String dashedPath = packageName.replaceAll('\\.', '/')
    File dir = this.project.mkdir("${basePath}/java/main/${dashedPath}")
    File outputFile = new File(dir.absolutePath, 'package-info.java')
    String templateOutput = getPackageInfoTemplate(packageName)

    outputFile.bytes = []
    outputFile << templateOutput
  }

  String buildPackageJavadoc() {
    def annotationList = this.annotations.get().collect { " *   <li>$it</li>" }.join('\n')
    def javadoc = !this.packageJavadoc.get().isEmpty()
      ? '\n| * \n|' + this.packageJavadoc.get().split('\n').collect { " * $it" }.join('\n')
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

  String getPackageInfoTemplate(packageName) {
    return """\
      |${buildPackageJavadoc()}
      |${this.annotations.get().collect { cannonicalToAnnotation(it) }.join('\n')}
      |package $packageName;

      |${this.annotations.get().collect { cannonicalToImport(it) }.join('\n')}
    |"""
    .stripMargin()
  }

  String cannonicalToAnnotation(String cannonical) {
    String className = cannonical.split(/\./).last()
    return "@$className"
  }

  String cannonicalToImport(String cannonical) {
    return "import $cannonical;"
  }
}
