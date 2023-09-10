package io.github.joselion.strictnullcheck;

import static org.assertj.core.api.Assertions.assertThat;
import static testing.Helpers.PROJECT_PATH;

import java.io.IOException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import testing.Helpers;
import testing.annotations.TestkitTest;

@TestkitTest class StrictNullCheckPluginTkTest {

  @Nested class when_the_plugin_is_applied {
    @Test void generates_package_info_files_before_compileJava_task() throws IOException {
      final var packageInfo = "build/generated/sources/strictNullCheck/java/main/com/example/app/package-info.java";
      Helpers.writeBuildGradle(
        """
        plugins {
          id "java"
          id "io.github.joselion.strict-null-check"
        }

        strictNullCheck {
          source {
            addFindBugs()
          }
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
         *   <li>javax.annotation.ParametersAreNonnullByDefault</li>
         * </ul>
         */
        @ParametersAreNonnullByDefault
        package com.example.app;

        import javax.annotation.ParametersAreNonnullByDefault;
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
            packageInfo.annotations = [
              'my.custom.annotation.NullApi',
              'my.custom.annotation.NullFields',
            ]
            source.dependencies = ['my.custom:annotations:1.5.3']
            source.addFindBugs()
          }

          repositories {
            mavenCentral()
          }

          task showConfig() {
            doLast {
              println("*** packageInfo.annotations: ${strictNullCheck.packageInfo.annotations.get()}")
              println("*** source.dependencies: ${strictNullCheck.source.dependencies.get()}")
            }
          }
          """
        );

        final var result = Helpers.runTask("showConfig");

        assertThat(result.getOutput())
          .contains("*** packageInfo.annotations: [my.custom.annotation.NullApi, my.custom.annotation.NullFields]")
          .contains("*** source.dependencies: [my.custom:annotations:1.5.3, com.google.code.findbugs:jsr305:+]")
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
            packageInfo {
              annotations = [
                'my.custom.annotation.NullApi',
                'my.custom.annotation.NullFields',
              ]
            }
            source {
              dependencies = ['my.custom:annotations:1.5.3']
              addFindBugs()
            }
          }

          repositories {
            mavenCentral()
          }

          task showConfig() {
            doLast {
              println("*** packageInfo.annotations: ${strictNullCheck.packageInfo.annotations.get()}")
              println("*** source.dependencies: ${strictNullCheck.source.dependencies.get()}")
            }
          }
          """
        );

        final var result = Helpers.runTask("showConfig");

        assertThat(result.getOutput())
          .contains("*** packageInfo.annotations: [my.custom.annotation.NullApi, my.custom.annotation.NullFields]")
          .contains("*** source.dependencies: [my.custom:annotations:1.5.3, com.google.code.findbugs:jsr305:+]")
          .contains("BUILD SUCCESSFUL");
      }
    }
  }
}
