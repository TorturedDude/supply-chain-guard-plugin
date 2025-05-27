plugins {
    `java-gradle-plugin`
    `maven-publish`

    id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

repositories {
    mavenCentral()
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useKotlinTest("1.9.10")
        }

        val functionalTest by registering(JvmTestSuite::class) {
            useKotlinTest("1.9.10")

            dependencies {
                implementation(project())
            }

            targets {
                all {
                    testTask.configure { shouldRunAfter(test) } 
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
