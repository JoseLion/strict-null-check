package testing;

import static testing.Helpers.PROJECT_PATH;
import static testing.Helpers.SRC_PATH;
import static testing.Helpers.TEST_PATH;

import java.nio.file.Files;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SetupRunner implements BeforeAllCallback {

  @Override
  public void beforeAll(final ExtensionContext context) throws Exception {
    SRC_PATH.toFile().mkdirs();
    TEST_PATH.toFile().mkdirs();

    Files.writeString(PROJECT_PATH.resolve("settings.gradle"), "");
    Files.writeString(
      SRC_PATH.resolve("MainApp.java"),
      """
      package com.example.app;

      public class MainApp {

        public static void main(final String[] args) {
        }
      }
      """
    );
  }
}
