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
      final var generatedDir = extension.getGeneratedDir().get();
      final var imports = extension.getPackageInfo().getImports().get();
      final var annotations = extension.getPackageInfo().getAnnotations().get();
      final var javadoc = extension.getPackageInfo().getJavadoc().get();
      final var dependencies = extension.getSource().getDependencies().get();

      assertThat(imports).containsExactly("javax.annotation.ParametersAreNonnullByDefault");
      assertThat(annotations).containsExactly("@ParametersAreNonnullByDefault");
      assertThat(generatedDir).isEqualTo(buildDir.concat("/generated/sources/strictNullCheck"));
      assertThat(javadoc).isEmpty();
      assertThat(dependencies).isEmpty();
    }
  }

  @Nested class packageInfo {
    @Nested class useSpring {
      @Test void sets_Spring_annotations() {
        final var project = ProjectBuilder.builder().build();
        final var extension = project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);
        final var packageInfo = extension.getPackageInfo();

        packageInfo.useSpring();

        assertThat(packageInfo.getImports().get()).containsExactly(
          "org.springframework.lang.NonNullApi",
          "org.springframework.lang.NonNullFields"
        );
        assertThat(packageInfo.getAnnotations().get()).containsExactly(
          "@NonNullApi",
          "@NonNullFields"
        );
      }
    }

    @Nested class useEclipse {
      @Test void sets_Eclipse_annotation() {
        final var project = ProjectBuilder.builder().build();
        final var extension = project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);
        final var packageInfo = extension.getPackageInfo();

        packageInfo.useEclipse();

        assertThat(packageInfo.getImports().get()).containsExactly(
          "static org.eclipse.jdt.annotation.DefaultLocation.ARRAY_CONTENTS",
          "static org.eclipse.jdt.annotation.DefaultLocation.FIELD",
          "static org.eclipse.jdt.annotation.DefaultLocation.PARAMETER",
          "static org.eclipse.jdt.annotation.DefaultLocation.RETURN_TYPE",
          "static org.eclipse.jdt.annotation.DefaultLocation.TYPE_ARGUMENT",
          "static org.eclipse.jdt.annotation.DefaultLocation.TYPE_BOUND",
          "static org.eclipse.jdt.annotation.DefaultLocation.TYPE_PARAMETER",
          "org.eclipse.jdt.annotation.NonNullByDefault"
        );
        assertThat(packageInfo.getAnnotations().get()).containsExactly(
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
        );
      }
    }
  }

  @Nested class source {
    @Nested class addFindBugs {
      @Test void adds_the_FindBugs_dependency() {
        final var project = ProjectBuilder.builder().build();
        final var extension = project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);

        extension.getSource().addFindBugs();

        final var dependencies = extension.getSource().getDependencies().get();

        assertThat(dependencies).contains("com.google.code.findbugs:jsr305:3.0.2");
      }
    }

    @Nested class addSpotBugs {
      @Test void adds_the_SpotBugs_dependency() {
        final var project = ProjectBuilder.builder().build();
        final var extension = project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);

        extension.getSource().addSpotBugs();

        final var dependencies = extension.getSource().getDependencies().get();

        assertThat(dependencies).contains("com.github.spotbugs:spotbugs-annotations:4.7.3");
      }
    }

    @Nested class addEclipse {
      @Test void adds_the_SpotBugs_dependency() {
        final var project = ProjectBuilder.builder().build();
        final var extension = project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);

        extension.getSource().addEclipse();

        final var dependencies = extension.getSource().getDependencies().get();

        assertThat(dependencies).contains("org.eclipse.jdt:org.eclipse.jdt.annotation:2.2.700");
      }
    }
  }
}
