package io.security.guard

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class SupplyChainGuardPluginFunctionalTest {

    @field:TempDir
    lateinit var projectDir: File

    private val buildFile by lazy { projectDir.resolve("build.gradle.kts") }
    private val settingsFile by lazy { projectDir.resolve("settings.gradle.kts") }

    @Test
    fun `generateSbom task generates expected report`() {
        settingsFile.writeText("rootProject.name = \"test-project\"")
        buildFile.writeText(
            """
            plugins {
                id("java")
                id("io.security.guard.greeting") // <-- твой ID из build.gradle.kts
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation("com.google.guava:guava:32.1.2-jre")
            }
            supplyChainGuard {
                reportPath = "reports/"
                failOnCritical = true
            }
            """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("generateSbom")
            .forwardOutput()
            .build()

        val sbomFile = projectDir.resolve("build/reports/sbom.json")

        assertTrue(sbomFile.exists(), "SBOM файл не был создан.")
        assertTrue(sbomFile.readText().contains("guava"), "SBOM должен содержать зависимость 'guava'.")
        assertTrue(result.output.contains("✅ SBOM written to"), "Ожидаемое сообщение не найдено в логах.")
    }

    @Test
    fun `scanCve task completes when no critical vulnerabilities found`() {
        settingsFile.writeText("rootProject.name = \"test-project\"")
        buildFile.writeText(
            """
        plugins {
            id("java")
            id("io.security.guard.greeting")
        }

        repositories {
            mavenCentral()
        }

        dependencies {
            implementation("com.google.guava:guava:32.1.2-jre")
        }

        supplyChainGuard {
            reportPath = "build/reports/"
            failOnCritical = true
        }
        """.trimIndent()
        )

        val sbomFile = File(projectDir, "build/reports/sbom.json")
        sbomFile.parentFile.mkdirs()
        sbomFile.writeText(
            """
        {
          "components": [
            {"group": "com.google.guava", "name": "guava", "version": "32.1.2-jre"}
          ]
        }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("scanCve")
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("Task :generateSbom"))
        assertTrue(result.output.contains("Task :scanCve"))
        assertTrue(result.output.contains("Vulnerabilities for"))
        assertTrue(!result.output.contains("Critical vulnerabilities found. Failing build."))
    }


    @Test
    fun `scanCve task fails build on critical vulnerability when failOnCritical is true`() {
        settingsFile.writeText("rootProject.name = \"test-project\"")
        buildFile.writeText(
            """
        plugins {
            id("java")
            id("io.security.guard.greeting")
        }

        repositories {
            mavenCentral()
        }

        supplyChainGuard {
            reportPath = "build/reports/"
            failOnCritical = true
        }
        """.trimIndent()
        )

        val sbomFile = File(projectDir, "build/reports/sbom.json")
        sbomFile.parentFile.mkdirs()
        sbomFile.writeText(
            """
        {
          "components": [
            {"group": "org.example", "name": "vulnerable-lib", "version": "1.0.0"}
          ]
        }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("scanCve")
            .forwardOutput()
            .buildAndFail()

        assertTrue(result.output.contains("Task :generateSbom"))
        assertTrue(result.output.contains("Task :scanCve"))
        assertTrue(result.output.contains("Critical vulnerabilities found. Failing build."))
    }


    @Test
    fun `scanCve task fails when sbom file is missing`() {
        settingsFile.writeText("rootProject.name = \"test-project\"")
        buildFile.writeText(
            """
        plugins {
            id("java")
            id("io.security.guard.greeting")
        }

        supplyChainGuard {
            reportPath = "build/reports/"
            failOnCritical = false
        }
        """.trimIndent()
        )

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("scanCve")
            .forwardOutput()
            .buildAndFail()

        assertTrue(result.output.contains("Task :generateSbom"))
        assertTrue(result.output.contains("Task :scanCve"))
        assertTrue(result.output.contains("SBOM file not found"))
    }


    @Test
    fun `scanCve task completes when no components found`() {
        settingsFile.writeText("rootProject.name = \"test-project\"")
        buildFile.writeText(
            """
        plugins {
            id("java")
            id("io.security.guard.greeting")
        }

        supplyChainGuard {
            reportPath = "reports/"
            failOnCritical = true
        }
        """.trimIndent()
        )

        val sbomDir = File(projectDir, "build/reports")
        sbomDir.mkdirs()
        File(sbomDir, "sbom.json").writeText("""{ "components": [] }""")

        val result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withPluginClasspath()
            .withArguments("scanCve")
            .forwardOutput()
            .build()

        assertTrue(result.output.contains("No components found in SBOM"))
    }
}
