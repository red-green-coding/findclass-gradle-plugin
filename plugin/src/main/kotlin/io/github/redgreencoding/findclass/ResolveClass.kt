package io.github.redgreencoding.findclass

import org.barfuin.texttree.api.DefaultNode
import org.barfuin.texttree.api.Node
import org.barfuin.texttree.internal.TextTreeImpl
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import java.net.URLClassLoader

@DisableCachingByDefault(because = "Task does not generate any output")
open class ResolveClass : DefaultTask() {
    init {
        description = "Resolves a given classname to find out which .jar is providing it"
        group = "help"

        notCompatibleWithConfigurationCache("not compatible with configuration cache")
    }

    @Input
    @Option(
        option = "classname",
        description = "full qualified classname. Example: org.apache.commons.lang3.StringUtils",
    )
    var classname: String? = null

    @Input
    @Option(
        option = "configurations",
        description = "Configurations that should be searched. Defaults to all. Example: runtimeClasspath,testRuntimeClasspath",
    )
    @Optional
    var configurations: List<String>? = null

    @TaskAction
    fun resolveClass() {
        classname?.let {
            logger.info("resolve class '{}'", classname)
            logger.info("search configurations '{}'", if (configurations == null) "all" else configurations)

            val node =
                findClassInConfigurations(it, project.configurations, configurations?.toSet() ?: emptySet())

            println(TextTreeImpl().render(node))
        }
    }

    private fun findClassInConfigurations(
        findClass: String,
        configurations: ConfigurationContainer,
        filterConfigurations: Set<String>,
    ): Node {
        val tree = DefaultNode("resolve class '$findClass':")

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
            }.toTypedArray(),
        )
}
