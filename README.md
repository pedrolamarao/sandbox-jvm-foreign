Date: 2021-05-17

# Summary

This project provides an experimental Java foreign function interface to Windows' API.

# Build

Requirements:

- Recent build from `panama-jextract` branch

To specify a custom JDK, create `gradle.properties` with content like:

```groovy
org.gradle.java.installations.auto-detect = false
org.gradle.java.installations.auto-download = false
org.gradle.java.installations.fromEnv =
org.gradle.java.installations.paths = E:/tools/cygwin64/home/pedro.lamarao/panama-foreign/build/windows-x86_64-server-release/images/jdk
```

To build:

`./gradlew build`
