package ru.golovanov.security.guard.extentions

import org.gradle.api.provider.Property
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

open class SupplyChainGuardExtension @Inject constructor(objects: ObjectFactory) {
    val sonatypeUsername: Property<String> = objects.property(String::class.java).convention(System.getenv("SONATYPE_USERNAME"))
    val sonatypeToken: Property<String> = objects.property(String::class.java).convention(System.getenv("SONATYPE_TOKEN"))
    val borderRateCveForFailure: Property<Double> = objects.property(Double::class.java).convention(7.5)
    val reportPath: Property<String> = objects.property(String::class.java).convention("reports/")
    val failOnCritical: Property<Boolean> = objects.property(Boolean::class.java).convention(false)
}