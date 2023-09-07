package io.github.joselion.strictnullcheck;

import static org.assertj.core.api.Assertions.assertThat;
import static testing.Helpers.PROJECT_PATH;

import java.io.IOException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import testing.Helpers;
import testing.annotations.TestkitTest;

@TestkitTest class StrictNullCheckPluginTest {

  @Nested class when_the_plugin_is_applied {
    @Test void generates_package_info_files_before_compileJava_task() throws IOException {
      final var packageInfo = "build/generated/sources/strictNullCheck/java/main/com/example/app/package-info.java";
      Helpers.writeBuildGradle(
        """
        plugins {
          id "java"
          id "io.github.joselion.strict-null-check"
        }

        repositories {
          mavenCentral()
        }
        """
      );

      final var result = Helpers.runTask("compileJava");

      assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
      assertThat(PROJECT_PATH.resolve(packageInfo).toFile()).content().isEqualTo(
        """
        /**
         * This package is checked for {@code null} by the following annotations:
         * <ul>
         *   <li>org.eclipse.jdt.annotation.NonNullByDefault</li>
         * </ul>
         */
        @NonNullByDefault
        package com.example.app;

        import org.eclipse.jdt.annotation.NonNullByDefault;
        """
      );
    }
  }

  @Nested class when_the_plugin_is_configured {
    @Nested class and_an_extension_property_is_changed {
      @Test void updates_the_plugin_configuration() {
        Helpers.writeBuildGradle(
          """
          plugins {
            id "java"
            id "io.github.joselion.strict-null-check"
          }

          strictNullCheck {
            annotations = [
              'my.custom.annotation.NullApi',
              'my.custom.annotation.NullFields'
            ]
            versions.findBugs = '1.0.0';
          }

          repositories {
            mavenCentral()
          }

          task showConfig() {
            doLast {
              println("*** annotations: ${strictNullCheck.annotations.get()}")
              println("*** versions.findBugs: ${strictNullCheck.versions.findBugs.get()}")
            }
          }
          """
        );

        final var result = Helpers.runTask("showConfig");

        assertThat(result.getOutput())
          .contains("*** annotations: [my.custom.annotation.NullApi, my.custom.annotation.NullFields]")
          .contains("*** versions.findBugs: 1.0.0")
          .contains("BUILD SUCCESSFUL");
      }
    }

    @Nested class and_closures_are_used_to_change_properties {
      @Test void updates_the_plugin_configuration() {
        Helpers.writeBuildGradle(
          """
          plugins {
            id "java"
            id "io.github.joselion.strict-null-check"
          }

          strictNullCheck {
            versions {
              eclipseAnnotations = '1.1.000'
              findBugs = '1.0.0'
            }
          }

          repositories {
            mavenCentral()
          }

          task showConfig() {
            doLast {
              println("*** varsions.eclipseAnnotations: ${strictNullCheck.versions.eclipseAnnotations.get()}")
              println("*** versions.findBugs: ${strictNullCheck.versions.findBugs.get()}")
            }
          }
          """
        );

        final var result = Helpers.runTask("showConfig");

        assertThat(result.getOutput())
          .contains("*** varsions.eclipseAnnotations: 1.1.000")
          .contains("*** versions.findBugs: 1.0.0")
          .contains("BUILD SUCCESSFUL");
      }
    }
  }
}
