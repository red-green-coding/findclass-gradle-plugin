package com.github.abendt.findclass

import org.barfuin.texttree.api.DefaultNode
import org.barfuin.texttree.api.Node
import org.barfuin.texttree.internal.TextTreeImpl
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.plugins.JavaPlugin
import java.net.URLClassLoader

class GradleFindclassPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withType(JavaPlugin::class.java) {
            project.tasks.register("findClass") { task ->
                task.doLast {
                    val findClass = project.property("fc.findClass") as String?

                    if (findClass != null) {
                        val tree = findClassInConfigurations(findClass, project.configurations)

                        println(TextTreeImpl().render(tree))
                    }
                }
            }
        }
    }

    private fun findClassInConfigurations(
        findClass: String,
        configurations: ConfigurationContainer
    ): Node {
        val tree = DefaultNode("find class '$findClass'")

        val deprecatedConfigurations = setOf("default", "archives")

        configurations
            .filter { !deprecatedConfigurations.contains(it.name) }
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

    private fun configToClassloader(config: Configuration): URLClassLoader {
        return URLClassLoader(
            config.resolve().map {
                it.toURI().toURL()
            }.toTypedArray()
        )
    }
}
