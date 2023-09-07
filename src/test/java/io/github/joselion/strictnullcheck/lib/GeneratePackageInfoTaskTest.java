package io.github.joselion.strictnullcheck.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import testing.annotations.UnitTest;

@UnitTest class GeneratePackageInfoTaskTest {

  @Nested class when_the_task_is_created {
    @Test void assigns_default_values() {
      final var project = ProjectBuilder.builder().build();
      final var extension = project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);
      final var defaultAnnotations = extension.getAnnotations().get();
      final var defaultGeneratedDir = extension.getGeneratedDir().get();
      final var defaultJavadoc = extension.getPackageJavadoc().get();
      final var task = project.getTasks().create("generatePackageInfo", GeneratePackageInfoTask.class);

      assertThat(task.getAnnotations().get()).isEqualTo(defaultAnnotations);
      assertThat(task.getOutputDir().get()).isEqualTo(defaultGeneratedDir);
      assertThat(task.getPackageJavadoc().get()).isEqualTo(defaultJavadoc);
    }
  }

  @Nested class getSourcePackages {
    @Nested class when_the_project_has_no_source_sets {
      @Test void returns_an_empty_set() {
        final var project = prepareProject();
        final var task = project.getTasks().create("generatePackageInfo", GeneratePackageInfoTask.class);

        assertThat(task.getSourcePackages()).isEmpty();
      }
    }
  }

  @Nested class getGeneratedDir {
    @Test void returns_a_file_using_the_outputDir_path() {
      final var project = prepareProject();
      final var task = project.getTasks().create("generatePackageInfo", GeneratePackageInfoTask.class);
      final var outputDir = task.getOutputDir().get();

      assertThat(task.getGeneratedDir()).isEqualTo(new File(outputDir));
    }
  }

  @Nested class generatePackageInfo {
    @Nested class when_default_options_are_used {
      @Test void generates_a_package_info_file_for_each_package() {
        final var project = prepareProject();
        final var packages = Set.of("com.test.foo", "com.test.bar", "com.test.foo.deep");
        final var task = spy(project.getTasks().create("generatePackageInfo", GeneratePackageInfoTask.class));

        when(task.getSourcePackages()).thenReturn(packages);
        task.generatePackageInfo();

        assertThat(packages).allSatisfy(packageName -> {
          final var path = Path.of(
            task.getOutputDir().get(),
            "/java/main",
            packageName.replace(".", "/"),
            "package-info.java"
          );
          final var content = Files.readString(path);

          assertThat(content).isEqualTo(
            """
            /**
             * This package is checked for {@code null} by the following annotations:
             * <ul>
             *   <li>org.eclipse.jdt.annotation.NonNullByDefault</li>
             * </ul>
             */
            @NonNullByDefault
            package %s;

            import org.eclipse.jdt.annotation.NonNullByDefault;
            """,
            packageName
          );
        });
      }
    }

    @Nested class when_annotations_are_provided {
      @Test void generates_the_package_info_using_those_annotation() throws IOException {
        final var project = prepareProject();
        final var task = spy(project.getTasks().create("generatePackageInfo", GeneratePackageInfoTask.class));

        when(task.getSourcePackages()).thenReturn(Set.of("com.test.foo"));
        task.getAnnotations().set(
          List.of(
            "org.springframework.lang.NonNullApi",
            "org.springframework.lang.NonNullFields"
          )
        );
        task.generatePackageInfo();

        final var path = Path.of(task.getOutputDir().get(), "/java/main/com/test/foo/package-info.java");
        final var content = Files.readString(path);

        assertThat(content).isEqualTo(
          """
          /**
           * This package is checked for {@code null} by the following annotations:
           * <ul>
           *   <li>org.springframework.lang.NonNullApi</li>
           *   <li>org.springframework.lang.NonNullFields</li>
           * </ul>
           */
          @NonNullApi
          @NonNullFields
          package com.test.foo;

          import org.springframework.lang.NonNullApi;
          import org.springframework.lang.NonNullFields;
          """
        );
      }
    }

    @Nested class when_the_outputDir_is_provided {
      @Test void generates_the_package_info_in_that_directory() {
        final var project = prepareProject();
        final var task = spy(project.getTasks().create("generatePackageInfo", GeneratePackageInfoTask.class));

        when(task.getSourcePackages()).thenReturn(Set.of("com.test.foo"));
        task.getOutputDir().set(
          project
            .getLayout()
            .getBuildDirectory()
            .getAsFile()
            .get()
            .getPath()
            .concat("some/other/place")
        );
        task.generatePackageInfo();

        final var path = Path.of(task.getOutputDir().get(), "/java/main/com/test/foo/package-info.java");

        assertThat(path).exists().isRegularFile();
      }
    }

    @Nested class when_the_packageJavadoc_is_provided {
      @Test void generates_the_package_info_with_that_javadoc() throws IOException {
        final var project = prepareProject();
        final var task = spy(project.getTasks().create("generatePackageInfo", GeneratePackageInfoTask.class));

        when(task.getSourcePackages()).thenReturn(Set.of("com.test.foo"));
        task.getPackageJavadoc().set(
          """
          @author JoseLion
          @since v1.1.0
          """
        );
        task.generatePackageInfo();

        final var path = Path.of(task.getOutputDir().get(), "/java/main/com/test/foo/package-info.java");
        final var content = Files.readString(path);

        assertThat(content).isEqualTo(
          """
          /**
           * This package is checked for {@code null} by the following annotations:
           * <ul>
           *   <li>org.eclipse.jdt.annotation.NonNullByDefault</li>
           * </ul>
           *
           * @author JoseLion
           * @since v1.1.0
           */
          @NonNullByDefault
          package com.test.foo;

          import org.eclipse.jdt.annotation.NonNullByDefault;
          """
        );
      }
    }
  }

  private Project prepareProject() {
    final var project = ProjectBuilder.builder().build();
    project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);
    project.getPlugins().apply(JavaPlugin.class);

    return project;
  }
}
