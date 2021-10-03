package io.github.redgreencoding.findclass

import org.barfuin.texttree.api.DefaultNode
import org.barfuin.texttree.api.Node
import org.barfuin.texttree.internal.TextTreeImpl
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Task does not generate any output")
open class ScanConfigurations @Inject constructor(private val archiveOperations: ArchiveOperations) :
    DefaultTask() {

    init {
        description = "Scans the configurations for a class pattern"
        group = "help"
    }

    @Input
    @Option(
        option = "pattern",
        description = "Pattern to scan for. Example: **/StringUtils.*"
    )
    var pattern: String? = null

    @Input
    @Option(
        option = "configurations",
        description = "Configurations that should be scanned. Defaults to all. Example: runtimeClasspath,testRuntimeClasspath"
    )
    @Optional
    var configurations: List<String>? = null

    @TaskAction
    fun scanConfigurations() {
        pattern?.let {
            logger.info("scan for pattern '{}'", pattern)
            logger.info("scan configurations '{}'", if (configurations == null) "all" else configurations)

            val node =
                searchClassPatternInConfigurations(
                    it,
                    project.configurations,
                    configurations?.toSet() ?: emptySet()
                )

            println(TextTreeImpl().render(node))
        }
    }

    private fun searchClassPatternInConfigurations(
        pattern: String,
        configurations: ConfigurationContainer,
        filterConfigurations: Set<String>
    ): Node {
        val deprecatedConfigurations = setOf("default", "archives")

        val tree = DefaultNode("scan configurations for pattern '$pattern':")

        configurations
            .filter { !deprecatedConfigurations.contains(it.name) }
            .filter { if (filterConfigurations.isEmpty()) true else filterConfigurations.contains(it.name) }
            .forEach { configuration ->
                if (configuration.isCanBeResolved) {
                    configuration.resolve().filter { it.name.endsWith(".jar") }
                        .forEach { file ->
                            val found = !archiveOperations.zipTree(file).matching {
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
}
