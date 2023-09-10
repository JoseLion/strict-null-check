package io.github.joselion.strictnullcheck;

import static org.assertj.core.api.Assertions.assertThat;

import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import io.github.joselion.strictnullcheck.lib.GeneratePackageInfoTask;
import io.github.joselion.strictnullcheck.lib.StrictNullCheckExtension;
import testing.annotations.UnitTest;

@UnitTest class StrictNullCheckPluginTest {

  @Nested class when_the_plugin_is_applied {
    @Test void creates_strictNullCheck_extension() {
      final var project = ProjectBuilder.builder().build();
      final var plugins = project.getPlugins();

      plugins.apply("java");
      plugins.apply("io.github.joselion.strict-null-check");

      final var extension = project.getExtensions().findByName("strictNullCheck");

      assertThat(extension)
        .isNotNull()
        .isInstanceOf(StrictNullCheckExtension.class);
    }

    @Test void registers_generatePackageInfo_task() {
      final var project = ProjectBuilder.builder().build();
      final var plugins = project.getPlugins();

      plugins.apply("java");
      plugins.apply("io.github.joselion.strict-null-check");

      final var generateTask = project.getTasks().findByName("generatePackageInfo");

      assertThat(generateTask)
        .isNotNull()
        .isInstanceOf(GeneratePackageInfoTask.class);
    }

    @Test void makes_compileJava_and_sourceJar_tasks_depend_on_generatePackageInfo_task() {
      final var project = ProjectBuilder.builder().build();
      final var plugins = project.getPlugins();

      plugins.apply("java");
      plugins.apply("io.github.joselion.strict-null-check");
      project.getExtensions().getByType(JavaPluginExtension.class).withSourcesJar();

      final var tasks = project.getTasks();
      final var generateTask = tasks.findByName("generatePackageInfo");
      final var compileJavaTask = tasks.findByName("compileJava");
      final var sourcesJarTask = tasks.findByName("sourcesJar");

      assertThat(generateTask).isNotNull();
      assertThat(compileJavaTask.getDependsOn()).contains(generateTask);
      assertThat(sourcesJarTask.getDependsOn()).contains(generateTask);
    }
  }
}
