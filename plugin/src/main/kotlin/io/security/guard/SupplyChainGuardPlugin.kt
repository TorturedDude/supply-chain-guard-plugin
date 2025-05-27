package io.security.guard

import io.security.guard.extentions.SupplyChainGuardExtension
import io.security.guard.tasks.GenerateSbomTask
import io.security.guard.tasks.ScanCveTask
import org.gradle.api.Project
import org.gradle.api.Plugin

class SupplyChainGuardPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        registerTasks(project, createExtension(project))
    }

    private fun createExtension(project: Project): SupplyChainGuardExtension =
        project.extensions.create(
            "supplyChainGuard",
            SupplyChainGuardExtension::class.java,
            project.objects
        )

    private fun registerTasks(project: Project, extension: SupplyChainGuardExtension): SupplyChainGuardPlugin {
        project.tasks.register("generateSbom", GenerateSbomTask::class.java) {
            it.reportPath.set(extension.reportPath)
            it.group = "security"
            it.description = "Generates a Software Bill of Materials (SBOM) for the project"
        }

        project.tasks.register("scanCve", ScanCveTask::class.java) {
            it.reportPath.set(extension.reportPath)
            it.failOnCritical.set(extension.failOnCritical)
            it.dependsOn("generateSbom")
        }

        return this
    }
}
