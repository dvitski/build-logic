package cc.dvitski.gradle.fabric

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.Property
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.the
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class FabricExtension(val project: Project) {
    abstract val modId: Property<String>
    abstract val minecraftVersion: Property<String>
    abstract val loaderVersion: Property<String>
    abstract val fabricApiVersion: Property<String>
    abstract val kotlinVersion: Property<String>
    abstract val fabricKotlinVersion: Property<String>

    init {
        project.pluginManager.apply("org.jetbrains.kotlin.jvm")

        project.afterEvaluate {
            pluginManager.apply("fabric-loom")

            project.configure<BasePluginExtension> {
                archivesName.set(modId)
            }

            project.version = "${project.version}+$minecraftVersion"

            pluginManager.withPlugin("fabric-loom") {
                dependencies {
                    add("minecraft", "com.mojang:minecraft:${minecraftVersion.get()}")
                    add("mappings", project.the<LoomGradleExtensionAPI>().officialMojangMappings())
                }
            }

            pluginManager.withPlugin("publishing") {
                configure<PublishingExtension> {
                    publications {
                        create("mavenJava", MavenPublication::class.java) {
                            artifactId = modId.get()
                            from(components.findByName("java"))
                        }
                    }
                }
            }

            dependencies {
                add("modImplementation", "net.fabricmc:fabric-loader:${loaderVersion.get()}")
                add("modImplementation", "net.fabricmc.fabric-api:fabric-api:${fabricApiVersion.get()}")
                add("modImplementation", "net.fabricmc:fabric-language-kotlin:${fabricKotlinVersion.get()}+kotlin.${kotlinVersion.get()}")
            }

            configure<LoomGradleExtensionAPI> {
                val file = file("src/main/resources/${modId.get()}.accesswidener")
                if (file.exists()) accessWidenerPath.set(file)

                splitEnvironmentSourceSets()

                mods {
                    create(modId.get()) {
                        sourceSet("main")
                        sourceSet("client")
                    }
                }
            }

            the<FabricApiExtension>().apply {
                configureDataGeneration {
                    client.set(true)
                }
            }

            tasks.named("processResources", ProcessResources::class.java) {
                inputs.property("version", version)
                filesMatching("fabric.mod.json") {
                    expand(mapOf("version" to version))
                }
            }

            tasks.withType<JavaCompile>().configureEach {
                options.release.set(21)
            }

            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_21)
                }
            }

            configure<JavaPluginExtension> {
                withSourcesJar()
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }

            tasks.named("jar", Jar::class.java) {
                from("LICENSE") {
                    rename { "${it}_${modId.get()}" }
                }
            }
        }
    }
}
