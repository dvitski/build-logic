package cc.dvitski.gradle.fabric

import org.gradle.api.Plugin
import org.gradle.api.Project

class FabricPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.logger.lifecycle("Fabric DSL: $DSL_VERSION")
        project.extensions.create("fabricDsl", FabricExtension::class.java, project)
    }

    companion object {
        val DSL_VERSION = FabricPlugin::class.java.getPackage().implementationVersion ?: "0.0.0+unknown"
    }
}
