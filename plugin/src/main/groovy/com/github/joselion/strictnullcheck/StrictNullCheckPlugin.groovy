package com.github.joselion.strictnullcheck

import org.gradle.api.Project
import org.gradle.api.Plugin

class StrictNullCheckPlugin implements Plugin<Project> {
  void apply(Project project) {
    project.tasks.register("greeting") {
      doLast {
        println("Hello from plugin 'com.github.joselion.strict-null-check'")
      }
    }
  }
}
