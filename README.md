# Strict Null Check Plugin
A Gradle plugin to add full **Strict Null Check** to your Java code ☕

## How it works
The `strict-null-check` plugin leverages _possible_ implementations of JSR-305 to add package-level annotations to your project that can add a non-null behavior to all variables, parameters, fields, etc. So in simple words, the plugin will automatically create all the `package-info.java` files for you with the configured annotation(s). And that's it! You can have strict null checks in java without ever worrying about creating all the `package-info.java` files for all your project packages.

Although, it's important to take into account that:
* The `package-info.java` files are generated right after the `classes` task, so you might need to compile (or run `./gradlew classes`) whenever you create a new package
* By default, the `package-info.java` files are created on the project's `build/` folder, under a `generated/` directory. However, this can be configured by the plugin's extension
* The plugin adds FindBug's JSR-305 as a `compileOnly` dependency. The version can be configured through the plugin's extension

## Requirements
* The [Java Plugin](https://docs.gradle.org/current/userguide/java_plugin.html) is required to:
  - Find the `classes` task
  - Find the `compileOnly` configuration

```gradle
plugins {
  id 'java'
}
```

## Usage
> **⚠️ BREAKING CHANGES**
>
> Due to changes on GitHub, and by consequence Gradle and Maven, it's no longer allowed to use `com.github` as a valid group ID prefix. That being said, from version v2.0.0 of the plugin the ID is now `io.github.joselion.strict-null-check`. If you want to use a version prior to v2.0.0 you can still find it under `com.github.joselion.strict-null-check`, but keep in mind that the `io.github` prefixed ID does not have any v1.x.x version available.

You can get the latest version from Gradle's plugins site:
https://plugins.gradle.org/plugin/io.github.joselion.strict-null-check

Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):
```gradle
plugins {
  id 'io.github.joselion.strict-null-check' version 'x.x.x'
}
```

Using [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application):
```gradle
buildscript {
  repositories {
    maven {
      url 'https://plugins.gradle.org/m2/'
    }
  }
  dependencies {
    classpath 'io.github.joselion:plugin:x.x.x'
  }
}

apply plugin: 'io.github.joselion.strict-null-check'
```

With the plugin applied, run `./gradlew classes` and the `package-info.java` file tree will be generated for you. And that's it! But remember, whenever a new package is created you'll need to run the `classes` task again.

**Note**: Running `./gradlew build` will also do the trick since the `build` task depends on the `compileJava` task, which depends on the `classes` task.

## Extension
The default extension configuration looks like this:
```gradle
strictNullCheck {
  annotations = ['org.eclipse.jdt.annotation.NonNullByDefault']
  generatedDir = "$buildDir/generated"
  versions {
    eclipseAnnotations = '2.2.600'
    findBugs = '3.0.2'
  }
}
```

### Extension properties
| Property                    | Default                                           | Description |
| --------------------------- | :-----------------------------------------------: | ----------- |
| annotation                  | `['org.eclipse.jdt.annotation.NonNullByDefault']` | List of fully qualified class names of the annotations to add to all the generated `package-info.java` files |
| generatedDir                | `"$buildDir/generated"`                           | The directory where the `package-info.java` files should be generated |
| packageJavadoc              | -                                                 | A single or multi-line string to add to the Javadoc of the generated `package-info.java` files |
| versions                    | -                                                 | A closure to configure the versions of the dependencies added |
| versions.findBugs           | `'3.0.2'`                                         | The `FindBugs JSR305` version to use. Check the [MVNRepo][1] for more versions |
| versions.eclipseAnnotations | `'2.2.600'`                                       | The `JDT Annotations` version to use. Check the [MVNRepo][2] for more versions |
| useSpring                   | -                                                 | A shortcut function to use Spring's null-safety annotation |

### The `useSpring()` shortcut
This function can be called inside the `strictNullCheck` closure as a shortcut to set Spring's null-safety annotations in the extension configuration. That is, setting:
```gradle
strictNullCheck {
  useSpring()
}
```

Is equivalent to:
```gradle
strictNullCheck {
  annotations = [
    'org.springframework.lang.NonNullApi',
    'org.springframework.lang.NonNullFields'
  ]
}
```

### Tasks
The plugin adds a task named `generatePackageInfo`. So if you want, you could also run `./gradlew generatePackageInfo` instead of running the `classes` task.

## Something's missing?
Please create an [issue](https://github.com/JoseLion/strict-null-check/issues/new) describing your request, feature, or bug. I'll try to look into it as soon as possible 🙂

## Contributions
Contributions are very welcome! To do so, please fork this repository and open a Pull Request to the `main` branch.

## License
[Apache License 2.0](LICENSE)

[1]: https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305
[2]: https://mvnrepository.com/artifact/org.eclipse.jdt/org.eclipse.jdt.annotation
