package io.github.redgreencoding.findclass

import org.barfuin.texttree.api.DefaultNode
import org.barfuin.texttree.api.Node
import org.barfuin.texttree.internal.TextTreeImpl
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.JavaPlugin
import java.net.URLClassLoader

class FindClassGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withType(JavaPlugin::class.java) {
            project.tasks.register("findClass") { task ->
                task.doLast {
                    val findClass = getProjectProperty(project, "fc.findClass")
                    val filterConfigurations = getFilterConfigurationsArg(project)

                    if (findClass != null) {
                        val tree = findClassInConfigurations(findClass, project.configurations, filterConfigurations)

                        println(TextTreeImpl().render(tree))
                    } else {
                        println("please specify a full qualified classname to look for using -Pfc.findClass=com.acme.MyClass")
                    }
                }
            }

            project.tasks.register("searchClass") { task ->
                task.doLast {
                    val searchClass = getProjectProperty(project, "fc.searchClass")
                    val filterConfigurations = getFilterConfigurationsArg(project)

                    if (searchClass != null) {
                        val tree = searchClassPatternInConfiguration(
                            project,
                            searchClass,
                            project.configurations,
                            filterConfigurations
                        )

                        println(TextTreeImpl().render(tree))
                    }
                }
            }
        }
    }

    private fun getProjectProperty(project: Project, property: String): String? =
        if (project.hasProperty(property)) project.property(property) as String else null

    private fun getProjectProperty(project: Project, property: String, default: String): String =
        if (project.hasProperty(property)) project.property(property) as String else default

    private fun getFilterConfigurationsArg(project: Project): Set<String> =
        getProjectProperty(project, "fc.configurations")?.split(",")?.toSet() ?: emptySet()

    private fun searchClassPatternInConfiguration(
        project: Project,
        pattern: String,
        configurations: ConfigurationContainer,
        filterConfigurations: Set<String>
    ): Node {
        val deprecatedConfigurations = setOf("default", "archives")

        val tree = DefaultNode("search class with pattern '$pattern'")

        configurations
            .filter { !deprecatedConfigurations.contains(it.name) }
            .filter { if (filterConfigurations.isEmpty()) true else filterConfigurations.contains(it.name) }
            .forEach { configuration ->
                if (configuration.isCanBeResolved) {
                    configuration.resolve().filter { it.name.endsWith(".jar") }
                        .forEach { file ->
                            val found = !project.zipTree(file).matching {
                                it.include(pattern)
                            }.isEmpty

                            if (found) {
                                val config = DefaultNode(configuration.name)
                                val foundAt = DefaultNode(file.absolutePath)

                                config.addChild(foundAt)
                                tree.addChild(config)
                            }
                        }
                }
            }

        return tree
    }

    private fun findClassInConfigurations(
        findClass: String,
        configurations: ConfigurationContainer,
        filterConfigurations: Set<String>
    ): Node {
        val tree = DefaultNode("find class '$findClass'")

        val deprecatedConfigurations = setOf("default", "archives")

        configurations
            .filter { !deprecatedConfigurations.contains(it.name) }
            .filter { if (filterConfigurations.isEmpty()) true else filterConfigurations.contains(it.name) }
            .forEach {
                if (it.isCanBeResolved) {
                    try {
                        val classLoader = configToClassloader(it)
                        val clazz = Class.forName(findClass, false, classLoader)
                        val foundAt = DefaultNode(clazz.protectionDomain.codeSource.location.toString())

                        val config = DefaultNode(it.name)

                        config.addChild(foundAt)
                        tree.addChild(config)
                    } catch (e: Exception) {
                    }
                }
            }
        return tree
    }

    private fun configToClassloader(config: Configuration): URLClassLoader =
        URLClassLoader(
            config.resolve().map {
                it.toURI().toURL()
            }.toTypedArray()
        )
}
