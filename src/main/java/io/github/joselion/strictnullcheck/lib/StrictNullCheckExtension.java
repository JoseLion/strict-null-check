package io.github.joselion.strictnullcheck.lib;

import java.util.List;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Nested;

import lombok.Getter;

@Getter
public class StrictNullCheckExtension {

  @Input
  private final Property<String> generatedDir;

  @Nested
  private final PackageInfo packageInfo;

  @Nested
  private final Source source;

  @Inject
  public StrictNullCheckExtension(final ObjectFactory objects, final ProjectLayout layout) {
    this.generatedDir = objects.property(String.class);
    this.packageInfo = objects.newInstance(PackageInfo.class);
    this.source = objects.newInstance(Source.class);

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

  public void source(final Action<Source> action) {
    action.execute(this.source);
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

  @Getter
  public static class Source {

    @Input
    private final ListProperty<String> dependencies;

    @Inject
    public Source(final ObjectFactory objects) {
      this.dependencies = objects.listProperty(String.class);

      this.dependencies.convention(List.of());
    }

    public void addFindBugs(final String version) {
      this.dependencies.add("com.google.code.findbugs:jsr305:".concat(version));
    }

    public void addFindBugs() {
      this.addFindBugs("+");
    }

    public void addSpotBugs(final String version) {
      this.dependencies.add("com.github.spotbugs:spotbugs-annotations:".concat(version));
    }

    public void addSpotBugs() {
      this.addSpotBugs("+");
    }

    public void addEclipse(final String version) {
      this.dependencies.add("org.eclipse.jdt:org.eclipse.jdt.annotation:".concat(version));
    }

    public void addEclipse() {
      this.addEclipse("+");
    }
  }
}
