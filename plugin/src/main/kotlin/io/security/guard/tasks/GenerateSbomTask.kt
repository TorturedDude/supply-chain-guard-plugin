package io.security.guard.tasks

import com.google.gson.GsonBuilder
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

open class GenerateSbomTask : DefaultTask() {

    @OutputFile
    val outputFile = project.objects.fileProperty().convention(
        project.layout.buildDirectory.file("reports/sbom.json")
    )

    @TaskAction
    fun generate() {
        val runtimeClasspath = project.configurations.getByName("runtimeClasspath").resolvedConfiguration

        val dependencies = runtimeClasspath.firstLevelModuleDependencies.flatMap { flattenDependencies(it) }
            .distinctBy { "${it.moduleGroup}:${it.moduleName}:${it.moduleVersion}" }

        val components = dependencies.map {
            mapOf(
                "group" to it.moduleGroup,
                "name" to it.moduleName,
                "version" to it.moduleVersion
            )
        }

        val result = mapOf("components" to components)

        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(result)

        outputFile.get().asFile.apply {
            parentFile.mkdirs()
            writeText(json)
        }

        println("✅ SBOM written to: ${outputFile.get().asFile.absolutePath}")
    }

    private fun flattenDependencies(dep: ResolvedDependency): List<ResolvedDependency> {
        return listOf(dep) + dep.children.flatMap { flattenDependencies(it) }
    }
}