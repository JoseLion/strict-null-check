package io.github.joselion.strictnullcheck;

import static org.gradle.api.plugins.JavaPlugin.COMPILE_JAVA_TASK_NAME;
import static org.gradle.api.tasks.SourceSet.MAIN_SOURCE_SET_NAME;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.tasks.SourceSetContainer;

import io.github.joselion.strictnullcheck.lib.GeneratePackageInfoTask;
import io.github.joselion.strictnullcheck.lib.StrictNullCheckExtension;

public class StrictNullCheckPlugin implements Plugin<Project> {

  @Override
  public void apply(final Project project) {
    final var extension = project.getExtensions().create("strictNullCheck", StrictNullCheckExtension.class);

    project.getPlugins().withType(JavaPlugin.class).configureEach(plugin -> {
      final var generateTask = project
        .getTasks()
        .register("generatePackageInfo", GeneratePackageInfoTask.class);

      project.getTasks().getByName(
        COMPILE_JAVA_TASK_NAME,
        task -> task.dependsOn(generateTask.get())
      );

      project.getTasks().whenTaskAdded(task -> {
        if (task.getName().equals("sourcesJar")) {
          task.dependsOn(generateTask.get());
        }
      });

      project
        .getExtensions()
        .getByType(SourceSetContainer.class)
        .getByName(MAIN_SOURCE_SET_NAME, sourceSet ->
          sourceSet
            .getJava()
            .srcDir(extension.getGeneratedDir().get().concat("/java/main"))
        );
    });

    project.afterEvaluate(evaluated -> {
      final var allCompileOnly = evaluated
        .getConfigurations()
        .matching(configuration -> {
          final var name = configuration.getName();
          return name.startsWith("compileOnly") || name.endsWith("CompileOnly");
        });

      allCompileOnly.configureEach(configuration ->
        extension
          .getSource()
          .getDependencies()
          .get()
          .stream()
          .map(evaluated.getDependencies()::create)
          .forEach(configuration.getDependencies()::add)
      );
    });
  }
}
