package testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

public class Helpers {

  public static final Path PROJECT_PATH = Path.of("build/testkit");

  public static final Path SRC_PATH = PROJECT_PATH.resolve("src/main/java/com/example/app");

  public static final Path TEST_PATH = PROJECT_PATH.resolve("src/test/java/com/example/app");

  public static void writeBuildGradle(final String text) {
    try {
      Files.writeString(PROJECT_PATH.resolve("build.gradle"), text);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to create build.gradle file", e);
    }
  }

  public static BuildResult runTask(final String... arguments) {
    final var runner = GradleRunner.create();
    runner.forwardOutput();
    runner.withPluginClasspath();
    runner.withArguments(arguments);
    runner.withProjectDir(PROJECT_PATH.toFile());

    return runner.build();
  }
}
