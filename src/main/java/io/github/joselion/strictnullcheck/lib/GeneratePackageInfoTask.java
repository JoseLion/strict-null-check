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
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
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

  @Input
  private final ListProperty<String> annotations;

  @Input
  private final Property<String> outputDir;

  @Input
  private final Property<String> packageJavadoc;

  @Internal
  private final Project project;

  @Inject
  public GeneratePackageInfoTask(final Project project) {
    final var extension = project.getExtensions().getByType(StrictNullCheckExtension.class);

    this.annotations = project.getObjects().listProperty(String.class);
    this.outputDir = project.getObjects().property(String.class);
    this.packageJavadoc = project.getObjects().property(String.class);
    this.project = project;

    this.annotations.convention(extension.getAnnotations());
    this.outputDir.convention(extension.getGeneratedDir());
    this.packageJavadoc.convention(extension.getPackageJavadoc());
  }

  @InputFiles
  public Set<String> getSourcePackages() {
    return this.project
      .getExtensions()
      .getByType(SourceSetContainer.class)
      .stream()
      .flatMap(sourceSet -> sourceSet.getAllJava().getSrcDirs().stream())
      .map(File::toPath)
      .filter(path -> !path.startsWith(getGeneratedDir().getPath()))
      .flatMap(path -> Maybe.just(path).resolve(Files::walk).orElseGet(Stream::empty))
      .filter(subdir -> Files.notExists(subdir.resolve("package-info.java")))
      .flatMap(subdir -> Maybe.just(subdir).resolve(Files::list).orElseGet(Stream::empty))
      .filter(path -> FileSystems.getDefault().getPathMatcher("glob:*.java").matches(path.getFileName()))
      .map(path ->
        Maybe.just(path)
          .resolve(Files::readString)
          .map(Pattern.compile("^package (.+);")::matcher)
          .orThrow(RuntimeException::new)
      )
      .filter(Matcher::find)
      .map(matcher -> matcher.group(1))
      .collect(Collectors.toSet());
  }

  @OutputDirectory
  public File getGeneratedDir() {
    return new File(this.outputDir.get());
  }

  @TaskAction
  public void generatePackageInfo() {
    this.getSourcePackages().forEach(this::buildPackageInfo);
  }

  private void buildPackageInfo(final String packageName) {
    final var basePath = this.getGeneratedDir().getPath();
    final var dashedPath = packageName.replace(".", "/");
    final var dir = this.project.mkdir(basePath.concat("/java/main/").concat(dashedPath));
    final var templateOutput = getPackageInfoTemplate(packageName);
    final var outputFile = new File(dir.getAbsolutePath(), "package-info.java");

    Maybe.just(outputFile)
      .resolve(File::createNewFile)
      .runEffect(wasCreated -> {
        if (wasCreated.booleanValue()) {
          Files.writeString(outputFile.toPath(), templateOutput);
        }
      })
      .orThrow(exception -> {
        project.getLogger().error(
          "Unable to generate package-info.java at [%s]".formatted(outputFile),
          exception
        );

        return new TaskExecutionException(this, exception);
      });
  }

  private String getPackageInfoTemplate(final String packageName) {
    final var javadoc = this.buildPackageJavadoc();
    final var annotationLines = this.annotations
      .get()
      .stream()
      .map(this::cannonicalToAnnotation)
      .collect(joining("\n"));
    final var importLines = this.annotations
      .get()
      .stream()
      .map(this::cannonicalToImport)
      .collect(joining("\n"));

    return """
      %s
      %s
      package %s;

      %s
      """
      .formatted(
        javadoc,
        annotationLines,
        packageName,
        importLines
      );
  }

  private String buildPackageJavadoc() {
    final var annotationList = this.annotations
      .get()
      .stream()
      .map(it -> " *   <li>".concat(it).concat("</li>"))
      .collect(joining("\n"));
    final var javadoc = !this.packageJavadoc.get().isEmpty()
      ? "\n *\n".concat(stream(this.packageJavadoc.get().split("\n")).map(" * "::concat).collect(joining("\n")))
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
        annotationList,
        javadoc
      );
  }

  private String cannonicalToAnnotation(final String cannonical) {
    final var names = cannonical.split("\\.");
    final var last = names[names.length - 1];

    return "@".concat(last);
  }

  private String cannonicalToImport(final String cannonical) {
    return "import ".concat(cannonical).concat(";");
  }
}
