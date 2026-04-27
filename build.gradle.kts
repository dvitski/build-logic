plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "cc.dvitski.gradle"
version = "2.0.0+fabric-loom.1.16"

gradlePlugin {
    plugins {
        register("fabric") {
            id = "$group.fabric"
            implementationClass = "$id.FabricPlugin"
        }
    }
}

repositories {
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
}

dependencies {
    implementation("net.fabricmc:fabric-loom:1.16-SNAPSHOT")
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:2.2.21")
}

publishing {
    repositories {
        val mavenUrl = System.getenv("MAVEN_URL")
        if (mavenUrl != null) {
            maven {
                name = "envmaven"
                url = uri(mavenUrl)
                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }
    }
}
