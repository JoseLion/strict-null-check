package io.github.joselion.strictnullcheck.lib;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import io.github.joselion.maybe.Maybe;
import lombok.Getter;

@Getter
public class GeneratePackageInfoTask extends DefaultTask {

  @Internal
  private final Project project;

  @Internal
  private final StrictNullCheckExtension extension;

  @Inject
  public GeneratePackageInfoTask(final Project project) {
    this.project = project;
    this.extension = project.getExtensions().getByType(StrictNullCheckExtension.class);
  }

  @InputFiles
  public Set<String> getSourcePackages() {
    return this.project
      .getExtensions()
      .getByType(SourceSetContainer.class)
      .stream()
      .flatMap(sourceSet -> sourceSet.getAllJava().getSrcDirs().stream())
      .map(File::toPath)
      .filter(path -> !path.startsWith(this.getGeneratedDir().getPath()))
      .flatMap(path -> Maybe.of(path).solve(Files::walk).orElseGet(Stream::empty))
      .filter(subdir -> Files.notExists(subdir.resolve("package-info.java")))
      .flatMap(subdir -> Maybe.of(subdir).solve(Files::list).orElseGet(Stream::empty))
      .filter(path -> FileSystems.getDefault().getPathMatcher("glob:*.java").matches(path.getFileName()))
      .map(path ->
        Maybe.of(path)
          .solve(Files::readString)
          .map(Pattern.compile("^package (.+);")::matcher)
          .orThrow(RuntimeException::new)
      )
      .filter(Matcher::find)
      .map(matcher -> matcher.group(1))
      .collect(Collectors.toSet());
  }

  @OutputDirectory
  public File getGeneratedDir() {
    final var outputDir = this.project
      .getExtensions()
      .getByType(StrictNullCheckExtension.class)
      .getGeneratedDir()
      .get();

    return new File(outputDir);
  }

  @TaskAction
  public void generatePackageInfo() {
    this.getSourcePackages().forEach(this::buildPackageInfo);
  }

  private void buildPackageInfo(final String packageName) {
    final var basePath = this.getGeneratedDir().getPath();
    final var dashedPath = packageName.replace(".", "/");
    final var dir = this.project.mkdir(basePath.concat("/java/main/").concat(dashedPath));
    final var templateOutput = this.getPackageInfoTemplate(packageName);
    final var outputFile = new File(dir.getAbsolutePath(), "package-info.java");

    Maybe.of(outputFile)
      .solve(File::createNewFile)
      .effect(wasCreated -> {
        if (wasCreated.booleanValue()) {
          Files.writeString(outputFile.toPath(), templateOutput);
        }
      })
      .orThrow(exception -> {
        this.project.getLogger().error(
          "Unable to generate package-info.java at [%s]".formatted(outputFile),
          exception
        );

        return new TaskExecutionException(this, exception);
      });
  }

  private String getPackageInfoTemplate(final String packageName) {
    final var packageInfo = this.extension.getPackageInfo();
    final var annotations = packageInfo.getAnnotations().get();
    final var imports = packageInfo.getImports().get();
    final var javadocComment = this.buildJavadocComment();
    final var annotationLines = annotations.stream().collect(joining("\n"));
    final var staticImports = imports
      .stream()
      .sequential()
      .filter(it -> it.contains("static "))
      .map(it -> "import ".concat(it).concat(";"))
      .collect(joining("\n"));
    final var staticImportLines = staticImports.isBlank()
      ? staticImports
      : staticImports.concat("\n\n");
    final var importLines = imports
      .stream()
      .filter(it -> !it.contains("static "))
      .map(it -> "import ".concat(it).concat(";"))
      .collect(joining("\n"));

    return """
      %s
      %s
      package %s;

      %s%s
      """
      .formatted(
        javadocComment,
        annotationLines,
        packageName,
        staticImportLines,
        importLines
      );
  }

  private String buildJavadocComment() {
    final var packageInfo = this.extension.getPackageInfo();
    final var imports = packageInfo.getImports().get();
    final var javadoc = packageInfo.getJavadoc().get();
    final var annotations = packageInfo
      .getAnnotations()
      .get()
      .stream()
      .map(it -> it.replace("@", ""))
      .map(it -> it.contains("(") ? it.substring(0, it.indexOf("(")) : it)
      .map(it -> imports.stream().filter(imp -> imp.contains(it)).findFirst().orElse(it))
      .map(it -> " *   <li>".concat(it).concat("</li>"))
      .collect(joining("\n"));
    final var javadocText = !javadoc.isEmpty()
      ? "\n *\n".concat(stream(javadoc.split("\n")).map(" * "::concat).collect(joining("\n")))
      : "";

    return """
      /**
       * This package is checked for {@code null} by the following annotations:
       * <ul>
      %s
       * </ul>%s
       */\
      """
      .formatted(
        annotations,
        javadocText
      );
  }
}
