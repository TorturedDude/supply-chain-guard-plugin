# Supply Chain Guard Gradle Plugin

![Build](https://img.shields.io/github/actions/workflow/status/TorturedDude/supply-chain-guard-plugin/develop-workflow.yaml?branch=develop)
![License](https://img.shields.io/github/license/TorturedDude/supply-chain-guard-plugin)

A lightweight Gradle plugin for securing your software supply chain by validating dependencies and integrating security best practices.

## ✨ Features

- Dependency validation and monitoring
- Integration with supply chain security tools
- Easily configurable in existing Gradle projects
- Kotlin-based for modern builds

## 📦 Installation

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("ru.golovanov.security.guard") version "0.0.2"
}
```
Make sure to include the GitHub Maven repository in your settings.gradle.kts or build.gradle.kts:

```kotlin
maven {
    url = uri("https://maven.pkg.github.com/TorturedDude/supply-chain-guard-plugin")
    credentials {
        username = System.getenv("USER_CI")
        password = System.getenv("TOKEN_CI")
    }
}
```

## 🚀 Usage

### ⚙️ Plugin Configuration

The plugin is configured in the `build.gradle.kts` file and supports the following parameters:

| Parameter                  | Type      | Description                                                                 | Default Value                    |
|----------------------------|-----------|-----------------------------------------------------------------------------|----------------------------------|
| `sonatypeUsername`         | `String`  | Email address registered on [Sonatype OSS Index](https://ossindex.sonatype.org/) | `System.getenv("SONATYPE_USERNAME")` |
| `sonatypeToken`            | `String`  | Access token obtained after registering with OSS Index                     | `System.getenv("SONATYPE_TOKEN")`    |
| `borderRateCveForFailure` | `Double`  | CVSS threshold (0.0–10.0); build will fail if exceeded                      | `7.5`                            |
| `reportPath`               | `String`  | Path to the directory where the vulnerability report will be saved         | `"reports/"`                     |
| `failOnCritical`           | `Boolean` | If `true`, the build will fail when at least one critical vulnerability (CVSS ≥ 9.0) is found | `false`                          |

> ℹ️ If a parameter is not explicitly set, the plugin will use its default value.
### 🔧 Example Configuration

```kotlin
supplyChainGuard {
    sonatypeUsername.set(System.getenv("SONATYPE_EMAIL"))
    sonatypeToken.set(System.getenv("SONATYPE_TOKEN"))
    borderRateCveForFailure.set(7.0)
    reportPath.set("reports/")
    failOnCritical.set(true)
}
```

## 📁 Project Structure
- src/ – source code of the plugin
- .github/workflows/ – CI/CD workflows
- build.gradle.kts – main Gradle build script

## 📜 License
This project is licensed under the MIT License. See LICENSE for details.

## 🙋‍♂️ Author
- Created by TorturedDude (https://github.com/TorturedDude)

## 📌 More detailed usage instructions will be added in future releases.