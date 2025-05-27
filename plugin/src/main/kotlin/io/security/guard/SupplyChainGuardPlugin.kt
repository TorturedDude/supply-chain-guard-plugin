package io.security.guard

import io.security.guard.tasks.GenerateSbomTask
import org.gradle.api.Project
import org.gradle.api.Plugin

class SupplyChainGuardPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("generateSbom", GenerateSbomTask::class.java) { task ->
            task.group = "security"
            task.description = "Generates a Software Bill of Materials (SBOM) for the project"
        }
    }
}
