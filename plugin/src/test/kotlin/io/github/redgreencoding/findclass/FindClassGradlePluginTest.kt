package io.github.redgreencoding.findclass

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test

class FindClassGradlePluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("io.github.redgreencoding.findclass")
    }
}
