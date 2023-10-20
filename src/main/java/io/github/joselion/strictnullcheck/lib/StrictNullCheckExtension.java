package io.github.joselion.strictnullcheck.lib;

import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;

import lombok.Getter;

@Getter
public class StrictNullCheckExtension {

  @Input
  private final Property<String> generatedDir;

  @Nested
  private final PackageInfo packageInfo;

  @Internal
  private final Project project;

  @Inject
  public StrictNullCheckExtension(final ObjectFactory objects, final ProjectLayout layout, final Project project) {
    this.generatedDir = objects.property(String.class);
    this.packageInfo = objects.newInstance(PackageInfo.class);
    this.project = project;

    this.generatedDir.convention(
      layout
        .getBuildDirectory()
        .getAsFile()
        .get()
        .getPath()
        .concat("/generated/sources/strictNullCheck")
    );
  }

  public void packageInfo(final Action<PackageInfo> action) {
    action.execute(this.packageInfo);
  }

  public void addFindBugs(final String version) {
    this.addDependency("com.google.code.findbugs:jsr305:".concat(version));
  }

  public void addFindBugs() {
    this.addFindBugs("3.0.2");
  }

  public void addSpotBugs(final String version) {
    this.addDependency("com.github.spotbugs:spotbugs-annotations:".concat(version));
  }

  public void addSpotBugs() {
    this.addSpotBugs("4.7.3");
  }

  public void addEclipse(final String version) {
    this.addDependency("org.eclipse.jdt:org.eclipse.jdt.annotation:".concat(version));
  }

  public void addEclipse() {
    this.addEclipse("2.2.700");
  }

  private void addDependency(final String notation) {
    final var dependency = this.project.getDependencies().create(notation);
    final var allCompileOnly = this.project
      .getConfigurations()
      .matching(config -> {
        final var name = config.getName();
        return name.startsWith("compileOnly") || name.endsWith("CompileOnly");
      });

    allCompileOnly.configureEach(config ->
      config
        .getDependencies()
        .add(dependency)
    );
  }

  @Getter
  public static class PackageInfo {

    @Input
    private final ListProperty<String> imports;

    @Input
    private final ListProperty<String> annotations;

    @Input
    private final Property<String> javadoc;

    @Inject
    public PackageInfo(final ObjectFactory objects) {
      this.imports = objects.listProperty(String.class);
      this.annotations = objects.listProperty(String.class);
      this.javadoc = objects.property(String.class);

      this.imports.convention(List.of("javax.annotation.ParametersAreNonnullByDefault"));
      this.annotations.convention(List.of("@ParametersAreNonnullByDefault"));
      this.javadoc.convention("");
    }

    public void useSpring() {
      this.imports.set(
        List.of(
          "org.springframework.lang.NonNullApi",
          "org.springframework.lang.NonNullFields"
        )
      );
      this.annotations.set(List.of("@NonNullApi", "@NonNullFields"));
    }

    public void useEclipse() {
      this.imports.set(
        List.of(
          "static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS",
          "static org.eclipse.jdt.annotation.DefaultLocation.FIELD",
          "static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER",
          "static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE",
          "static org.eclipse.jdt.annotation.DefaultLocation.TYPE_ARGUMENT",
          "static org.eclipse.jdt.annotation.DefaultLocation.TYPE_BOUND",
          "static org.eclipse.jdt.annotation.DefaultLocation.TYPE_PARAMETER",
          "org.eclipse.jdt.annotation.NonNullByDefault"
        )
      );
      this.annotations.set(
        List.of(
          """
          @NonNullByDefault({
            ARRAY_CONTENTS,
            FIELD, PARAMETER,
            RETURN_TYPE,
            TYPE_ARGUMENT,
            TYPE_BOUND,
            TYPE_PARAMETER
          })\
          """
        )
      );
    }
  }
}
