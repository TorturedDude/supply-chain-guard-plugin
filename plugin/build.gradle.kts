plugins {
    `java-gradle-plugin`
    `maven-publish`

    id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

testing {
    suites {
        val functionalTest by registering(JvmTestSuite::class) {
            useKotlinTest("1.9.10")

            dependencies {
                implementation(project())
            }

            targets {
                all {

                }
            }
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/OWNER/REPOSITORY")
            credentials {
                username = (project.findProperty("gpr.user") ?: System.getenv("USERNAME")) as String?
                password = (project.findProperty("gpr.key") ?: System.getenv("TOKEN")) as String?
            }
        }
    }
    publications {
        register<MavenPublication>("gpr") {
            groupId = "ru.golovanov.security.guard"
            artifactId = "supply-chain-guard-gradle-plugin"
            version = "0.0.9"
        }
    }
}

gradlePlugin {
    val supplyChainGuardGradlePlugin by plugins.creating {
        id = "ru.golovanov.security.guard.supply-chain-guard-gradle-plugin"
        implementationClass = "io.security.guard.SupplyChainGuardPlugin"
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("functionalTest"))
}
