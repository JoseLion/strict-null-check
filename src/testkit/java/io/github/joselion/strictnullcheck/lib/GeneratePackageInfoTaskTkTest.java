package io.github.joselion.strictnullcheck.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static testing.Helpers.PROJECT_PATH;
import static testing.Helpers.SRC_PATH;
import static testing.Helpers.TEST_PATH;

import java.io.IOException;
import java.nio.file.Files;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import testing.Helpers;
import testing.annotations.TestkitTest;

@TestkitTest class GeneratePackageInfoTaskTkTest {

  @Nested class when_the_task_runs {
    @Test void generates_package_info_file_if_they_dont_aready_exist() throws IOException {
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
      SRC_PATH.resolve("other").toFile().mkdirs();
      TEST_PATH.resolve("other").toFile().mkdirs();
      Files.writeString(
        SRC_PATH.resolve("other/package-info.java"),
        """
        package com.example.app.other;
        """
      );
      Files.writeString(
        SRC_PATH.resolve("other/Other.java"),
        """
        package com.example.app.other;

        public class Other {
        }
        """
      );

      final var result = Helpers.runTask("compileJava");
      final var basePath = PROJECT_PATH.resolve("build/generated/sources/strictNullCheck/java/main/com/example/app");

      assertThat(result.getOutput()).contains("BUILD SUCCESSFUL");
      assertThat(basePath.resolve("package-info.java").toFile()).exists();
      assertThat(basePath.resolve("other/package-info.java").toFile()).doesNotExist();
    }
  }
}
