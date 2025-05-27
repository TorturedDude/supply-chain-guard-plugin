plugins {
    `java-gradle-plugin`

    id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

repositories {
    mavenCentral()
}

dependencies {
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

gradlePlugin {
    val greeting by plugins.creating {
        id = "io.security.guard.greeting"
        implementationClass = "io.security.guard.SupplyChainGuardPlugin"
    }
}

gradlePlugin.testSourceSets.add(sourceSets["functionalTest"])

tasks.named<Task>("check") {
    dependsOn(testing.suites.named("functionalTest"))
}
