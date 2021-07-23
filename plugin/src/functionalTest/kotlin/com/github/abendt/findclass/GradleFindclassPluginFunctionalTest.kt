package com.github.abendt.findclass

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.test.Test
import kotlin.test.assertTrue

class GradleFindclassPluginFunctionalTest {
    @Test fun `can run findClass task`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle").writeText("""
            plugins {
                id 'java'
                id('com.github.abendt.findclass')
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation 'org.apache.commons:commons-lang3:3.11'
            }
        """)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("findClass", "-Pfc.findClass=org.apache.commons.lang3.StringUtils", "--warning-mode",  "all")
        runner.withProjectDir(projectDir)
        val result = runner.build();

        // Verify the result
        assertTrue(result.output.contains("commons-lang3-3.11.jar"))
    }
}
