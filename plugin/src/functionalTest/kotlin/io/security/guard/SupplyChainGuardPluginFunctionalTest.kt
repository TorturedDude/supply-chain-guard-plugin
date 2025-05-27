package io.security.guard

import java.io.File
import kotlin.test.assertTrue
import kotlin.test.Test
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir

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
}
