package io.security.guard.extentions

import org.gradle.api.provider.Property
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class SupplyChainGuardExtension @Inject constructor(objects: ObjectFactory) {
    val reportPath: Property<String> = objects.property(String::class.java).convention("reports/")
    val failOnCritical: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
}