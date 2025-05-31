plugins {
    `java-gradle-plugin`
    `maven-publish`

    id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

group = "ru.golovanov.security.guard"
version = "0.0.2"

val githubUser = System.getenv("USER_CI")
val githubToken = System.getenv("TOKEN_CI")

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

gradlePlugin {
    plugins {
        create("supplyChainGuard") {
            id = "ru.golovanov.security.guard"
            implementationClass = "ru.golovanov.security.guard.SupplyChainGuardPlugin"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/TorturedDude/supply-chain-guard-plugin")
            credentials {
                username = githubUser
                password = githubToken
            }
        }
    }
}
