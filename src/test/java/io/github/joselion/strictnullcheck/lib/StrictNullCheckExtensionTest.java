package io.github.joselion.strictnullcheck.lib;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import testing.annotations.UnitTest;

@UnitTest class StrictNullCheckExtensionTest {

  @Nested class when_the_extension_is_created {
    @Test void assigns_default_values() {
      final var project = ProjectBuilder.builder().build();
      final var extension = project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);
      final var buildDir = project.getLayout().getBuildDirectory().get().toString();
      final var annotations = extension.getAnnotations().get();
      final var generatedDir = extension.getGeneratedDir().get();
      final var packageJavadoc = extension.getPackageJavadoc().get();
      final var versions = extension.getVersions();

      assertThat(annotations).containsExactly("org.eclipse.jdt.annotation.NonNullByDefault");
      assertThat(generatedDir).isEqualTo(buildDir.concat("/generated/sources/strictNullCheck"));
      assertThat(packageJavadoc).isEmpty();
      assertThat(versions.getEclipseAnnotations().get()).isEqualTo("2.2.600");
      assertThat(versions.getFindBugs().get()).isEqualTo("3.0.2");
    }
  }

  @Nested class useSpring {
    @Test void sets_Spring_annotations() {
      final var project = ProjectBuilder.builder().build();
      final var extension = project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);

      extension.useSpring();

      assertThat(extension.getAnnotations().get()).containsExactly(
        "org.springframework.lang.NonNullApi",
        "org.springframework.lang.NonNullFields"
      );
    }
  }
}
