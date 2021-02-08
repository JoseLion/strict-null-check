package com.github.joselion.strictnullcheck

import java.util.List

public class StrictNullCheckExtension {

  List<String> annotations

  String generatedDir

  String findbugsVersion

  public StrictNullCheckExtension(
    List<String> annotations,
    String generatedDir,
    String findbugsVersion
  ) {
    this.annotations = annotations
    this.generatedDir = generatedDir
    this.findbugsVersion = findbugsVersion
  }
}
