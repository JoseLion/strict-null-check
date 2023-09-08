package io.github.joselion.strictnullcheck;

import java.util.List;

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
      final var configuration = project.getConfigurations().getByName("compileOnly");
      final var findbugsVersion = extension.getVersions().getFindBugs().get();
      final var eclipseVersion = extension.getVersions().getEclipseAnnotations().get();

      configuration.getDependencies().addAll(
        List.of(
          project.getDependencies().create("com.google.code.findbugs:jsr305:".concat(findbugsVersion)),
          project.getDependencies().create("org.eclipse.jdt:org.eclipse.jdt.annotation:".concat(eclipseVersion))
        )
      );

      final var generateTask = project.getTasks().register("generatePackageInfo", GeneratePackageInfoTask.class);

      project.getTasks().getByName(
        JavaPlugin.COMPILE_JAVA_TASK_NAME,
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
        .getByName("main")
        .getAllJava()
        .srcDir(extension.getGeneratedDir().get().concat("/java/main"));
    });
  }
}
