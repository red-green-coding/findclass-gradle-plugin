package io.github.redgreencoding.findclass

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

class FindClassGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withType(JavaPlugin::class.java) {
            project.tasks.register("resolveClass", ResolveClass::class.java)
            project.tasks.register("scanConfigurations", ScanConfigurations::class.java)
        }
    }
}
