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
  private final ListProperty<String> annotations;

  @Input
  private final Property<String> generatedDir;

  @Input
  private final Property<String> packageJavadoc;

  @Nested
  private final Versions versions;

  @Inject
  public StrictNullCheckExtension(final ObjectFactory objects, final ProjectLayout layout) {
    this.annotations = objects.listProperty(String.class);
    this.generatedDir = objects.property(String.class);
    this.packageJavadoc = objects.property(String.class);
    this.versions = objects.newInstance(Versions.class);

    this.annotations.convention(List.of("org.eclipse.jdt.annotation.NonNullByDefault"));
    this.generatedDir.convention(
      layout
        .getBuildDirectory()
        .getAsFile()
        .get()
        .getPath()
        .concat("/generated/sources/strictNullCheck")
    );
    this.packageJavadoc.convention("");
  }

  public void useSpring() {
    this.annotations.convention(
      List.of(
        "org.springframework.lang.NonNullApi",
        "org.springframework.lang.NonNullFields"
      )
    );
  }

  public void versions(final Action<Versions> action) {
    action.execute(this.versions);
  }

  @Getter
  public static class Versions {

    @Input
    private final Property<String> eclipseAnnotations;

    @Input
    private final Property<String> findBugs;

    @Inject
    public Versions(final ObjectFactory objects) {
      this.eclipseAnnotations = objects.property(String.class);
      this.findBugs = objects.property(String.class);

      this.eclipseAnnotations.convention("2.2.600");
      this.findBugs.convention("3.0.2");
    }
  }
}
