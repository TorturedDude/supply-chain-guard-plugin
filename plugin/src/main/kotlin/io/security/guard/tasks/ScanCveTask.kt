package io.security.guard.tasks

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.security.guard.dtos.Component
import io.security.guard.dtos.OssIndexResult
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ScanCveTask : DefaultTask() {

    @get:Input
    abstract val failOnCritical: Property<Boolean>

    @get:Input
    abstract val reportPath: Property<String>

    @get:InputFile
    abstract val sbomFile: RegularFileProperty

    private val gson = Gson()
    private val client = OkHttpClient()

    init {
        sbomFile.set(
            project.layout.buildDirectory.file(reportPath.map { "$it/sbom.json" })
        )
    }

    @TaskAction
    fun scan() {
        val file = sbomFile.get().asFile
        if (!file.exists()) {
            throw GradleException("SBOM file not found: ${file.absolutePath}. Run generateSbom first.")
        }

        val components = readComponents(file)
        if (components.isEmpty()) {
            logger.lifecycle("No components found in SBOM. Skipping scan.")
            return
        }

        val coordinates = components.map { "pkg:maven/${it.group}/${it.name}@${it.version}" }
        val results = fetchVulnerabilities(coordinates)
        val hasCritical = reportResults(results)

        if (hasCritical && failOnCritical.get() == true) {
            throw GradleException("Critical vulnerabilities found. Failing build.")
        }
    }

    private fun readComponents(file: File): List<Component> {
        if (!file.exists()) {
            throw GradleException("SBOM file not found: ${file.absolutePath}. Run generateSbom first.")
        }

        val json = file.readText()
        val parsed = gson.fromJson<Map<String, List<Map<String, String>>>>(
            json,
            object : TypeToken<Map<String, List<Map<String, String>>>>() {}.type
        )

        return parsed["components"].orEmpty().map {
            Component(
                group = it["group"] ?: error("Missing group"),
                name = it["name"] ?: error("Missing name"),
                version = it["version"] ?: error("Missing version")
            )
        }
    }

    private fun fetchVulnerabilities(coordinates: List<String>): List<OssIndexResult> {
        val jsonBody = gson.toJson(mapOf("coordinates" to coordinates))
        val mediaType = "application/json".toMediaType()
        val request = Request.Builder()
            .url("https://ossindex.sonatype.org/api/v3/component-report")
            .post(jsonBody.toRequestBody(mediaType))
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw GradleException("OSS Index API failed: HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw GradleException("Empty OSS Index response body")
            val listType = object : TypeToken<List<OssIndexResult>>() {}.type
            return gson.fromJson(body, listType)
        }
    }

    private fun reportResults(results: List<OssIndexResult>): Boolean {
        var hasCritical = false

        for (entry in results) {
            if (entry.vulnerabilities.isEmpty()) continue

            println("\n🔍 Vulnerabilities for ${entry.coordinates}:")
            for (vulnerability in entry.vulnerabilities) {
                val score = vulnerability.cvssScore ?: 0.0
                val cve = vulnerability.cve ?: "N/A"
                val title = vulnerability.title ?: "Unknown"
                println(" - [$score] $title ($cve)")

                if (score >= 9.0) {
                    hasCritical = true
                }
            }
        }

        return hasCritical
    }
}