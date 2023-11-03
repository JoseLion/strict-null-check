[![CI](https://github.com/JoseLion/strict-null-check/actions/workflows/ci.yml/badge.svg)](https://github.com/JoseLion/strict-null-check/actions/workflows/ci.yml)
[![CodeQL](https://github.com/JoseLion/strict-null-check/actions/workflows/codeql.yml/badge.svg)](https://github.com/JoseLion/strict-null-check/actions/workflows/codeql.yml)
[![Release](https://github.com/JoseLion/strict-null-check/actions/workflows/release.yml/badge.svg)](https://github.com/JoseLion/strict-null-check/actions/workflows/release.yml)
[![Pages](https://github.com/JoseLion/strict-null-check/actions/workflows/pages.yml/badge.svg)](https://github.com/JoseLion/strict-null-check/actions/workflows/pages.yml)
[![Gradle Plugin](https://img.shields.io/gradle-plugin-portal/v/io.github.joselion.strict-null-check?logo=gradle)](https://plugins.gradle.org/plugin/io.github.joselion.strict-null-check)
[![License](https://img.shields.io/github/license/JoseLion/strict-null-check)](./LICENSE)
[![Known Vulnerabilities](https://snyk.io/test/github/JoseLion/strict-null-check/badge.svg)](https://snyk.io/test/github/JoseLion/strict-null-check)

# Strict Null Check Plugin

A Gradle plugin to make your Java ☕ code check for **nullability** by default

## How it works

The `strict-null-check` plugin leverages _possible_ implementations of JSR-305 to add package-level annotations to your project that can add a non-null behavior to all variables, parameters, fields, etc. So in simple words, the plugin will automatically create all the `package-info.java` files for you with the configured annotation(s). And that's it! Now tools like [Sonarlint](https://www.sonarsource.com/products/sonarlint/) can easily do their job by checking the package level annotations against other "nullability" annotations.

It's important to take into account the following:
* The plugin requires Gradle's [Java](https://docs.gradle.org/current/userguide/java_plugin.html) plugin to work as expected.
* The plugin creates a `generatePackageInfo` tasks when applied. This task is in charge of automatically generating the `package-info.java` files.
* The `generatePackageInfo` task is rigged to run before the `compileJava` and `sourcesJar` tasks, so you don't need worry about manually running it upon build. 
* By default, the `package-info.java` files are created on the project's `build/` folder, under a `generated/` directory. However, this can be configured using the plugin's extension.

## Install

> **⚠️ BREAKING CHANGES ⚠️**
>
> Due to changes on GitHub, and by consequence Gradle and Maven, it's no longer allowed to use `com.github` as a valid group ID prefix. That being said, from version v2.0.0 of the plugin the ID is now `io.github.joselion.strict-null-check`. If you want to use a version prior to v2.0.0 you can still find it under `com.github.joselion.strict-null-check`, but keep in mind that the `io.github` prefixed ID does not have any v1.x.x version available.

You can get the latest version from Gradle's plugins site:
https://plugins.gradle.org/plugin/io.github.joselion.strict-null-check

Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):

```groovy
plugins {
  id 'io.github.joselion.strict-null-check' version 'x.x.x'
}
```

Using [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application):

```groovy
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

## Usage

The default extension configuration only adds the `javax.annotation.ParametersAreNonnullByDefault` annotation to the package. However, this annotation may not be enough for many, plus you'll need to have a _possible_ implementation on JSR-305 on the classpath. It's highly recommended to use the following configuration to get the most out of nullability checks:

```groovy
strictNullCheck {
  addEclipse()
  packageInfo {
    useEclipse()
  }
}
```

Eclise's annotation are recommended because they have the most applicable locations compared to others, plus you may not need a _possible_ JSR-305 implementation if used solely with Sonarlint. However, if you're working with Spring Framework, you can also use the annotation they provide along with FindBugs or SpotBugs:

```groovy
strictNullCheck {
  addFindBugs()
  // addSpotBugs()
  packageInfo {
    useSpring()
  }
}
```

### Custom annotation

If none of the shorcut function work for you, you can always customize the generation as needed. You'll need to add the your custom annotation as a dependency as usual:

```groovy
strictNullCheck {
  addSpotBugs()
  generatedDir = "$buildDir/custom/classpath/dir"
  packageInfo {
    imports = ['my.custom.annotation.NonNullByDefault']
    annotation = ['@NonNullByDefault']
    javadoc = "@since v1.0.0"
  }
}

dependencies {
  compileOnly('my.custome.annotation:jsr-305:1.3.8')
}
```

### Extension API

| Property                 | Default                                              | Description |
| ------------------------ | ---------------------------------------------------- | ----------- |
| generatedDir             | `"$buildDir/generated/sources/strictNullCheck"`      | The directory where the classpath of the `package-info.java` files will be generated |
| addEclipse(version)      | version = `2.2.700`                                  | Shortcut function to add `org.eclipse.jdt:org.eclipse.jdt.annotation` as a **compileOnly** dependency |
| addFindBugs(version)     | version = `3.0.2`                                    | Shortcut function to add `com.google.code.findbugs:jsr305` as a **compileOnly** dependency |
| addSpotBugs(version)     | version = `4.7.3`                                    | Shortcut function to add `com.github.spotbugs:spotbugs-annotations` as a **compileOnly** dependency |
| packageInfo              | -                                                    | Container to configure package-info related setting |
| packageInfo.imports      | `['javax.annotation.ParametersAreNonnullByDefault']` | List of fully qualified imports to be added to the `package-info.java` files. Static imports can be added using `static ` as prefix |
| packageInfo.annotations  | `['@ParametersAreNonnullByDefault']`                 | List of java code annotations to be added to the `package-info.java` files |
| packageInfo.javadoc      | `''`                                                 | Additional text to be added to the javadoc of the `package-info.java` files |
| packageInfo.useSpring()  | -                                                    | Shorcut function to set Spring's imports and annotations |
| packageInfo.useEclipse() | -                                                    | Shorcut function to set Eclipse's imports and annotations |

### Overriding package-info.java

Having more than one `package-info.java` file on the same package classpath may cause issues during compilation. To avoid this, the plugin **will not** generate a `package-info.java` file if it already exists. This is also convenient if you'd like to override the annotations or javadoc on a specific package.

The only pitfall of this behavior is that you might need to clean the `build/` folder if a `package-info.java` file was previously generated. Also, if you just want to add more annotations to the package additional to the ones added by the plugin, you'll have to add the extra annotations and ones added by the plugin, for example:
```sh
./gradlew clean
```

Now we overide a `package-info.java` file.
```java
// We add an extra annotation
@Deprecated
// But we want to keep the nullability annotations for this package
@NonNullApi
@NonNullFields
package com.acme.app;

import java.lang.Deprecated;

import org.springframework.lang.NonNullApi;
import org.springframework.lang.NonNullFields;
```
## Something's missing?

Please create an [issue](https://github.com/JoseLion/strict-null-check/issues/new) describing your request, feature, or bug. I'll try to look into it as soon as possible 🙂

## Contributions

Contributions are very welcome! To do so, please fork this repository and open a Pull Request to the `main` branch.

## License

[Apache License 2.0](LICENSE)
