package io.github.redgreencoding.findclass

import io.kotest.core.spec.style.StringSpec
import org.gradle.testfixtures.ProjectBuilder

class FindClassGradlePluginTest : StringSpec({
    "plugin registers task" {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.redgreencoding.findclass")
    }

    "foo" {
        println(Class.forName("kotlin.time.DurationKt").protectionDomain.codeSource.location)
    }
})
