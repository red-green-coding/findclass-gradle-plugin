package io.github.redgreencoding.findclass

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import org.junit.Before
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FindClassGradlePluginFunctionalTest {

    val projectDir = File("build/functionalTest")

    @Before
    fun setUp() {
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle").writeText("")
    }

    @Test
    fun `can run findClass task`() {
        // Setup the test build
        projectDir.resolve("build.gradle").writeText(
            """
            plugins {
                id 'java'
                id('io.github.redgreencoding.findclass')
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation 'org.apache.commons:commons-lang3:3.11'
            }
        """
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(
            "findClass",
            "-Pfc.findClass=org.apache.commons.lang3.StringUtils",
            "--warning-mode",
            "all"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("commons-lang3-3.11.jar"))
    }

    @Test
    fun `can use as init script`() {
        val gradleHomeDir = File("build/gradleHome")
        gradleHomeDir.mkdirs()

        val initDir = File(gradleHomeDir, "init.d")
        initDir.mkdirs()

        initDir.resolve("findClass.gradle").writeText(
            """initscript {
                    dependencies {
                        classpath files(${PluginUnderTestMetadataReading.readImplementationClasspath().joinToString(separator = ",") { "'$it'" }})
                    }
                }
                rootProject {
                    apply plugin: io.github.redgreencoding.findclass.FindClassGradlePlugin
                }
            """.trimIndent()
        )

        // Setup the test build
        projectDir.resolve("build.gradle").writeText(
            """
            plugins {
                id 'java'
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation 'org.apache.commons:commons-lang3:3.11'
            }
        """
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(
            "-Dgradle.user.home=${gradleHomeDir.absolutePath}",
            "findClass",
            "-Pfc.findClass=org.apache.commons.lang3.StringUtils",
            "--warning-mode",
            "all"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("commons-lang3-3.11.jar"))
    }

    @Test
    fun `findClass can filter out configurations`() {
        projectDir.resolve("build.gradle").writeText(
            """
            plugins {
                id 'java'
                id('io.github.redgreencoding.findclass')
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation 'org.apache.commons:commons-lang3:3.11'
            }
        """
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(
            "findClass",
            "-Pfc.findClass=org.apache.commons.lang3.StringUtils",
            "-Pfc.configurations=runtimeClasspath",
            "--warning-mode",
            "all"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("commons-lang3-3.11.jar"))
        assertFalse(result.output.contains("testCompileClasspath"))
        assertFalse(result.output.contains("testRuntimeClasspath"))
    }

    @Test
    fun `can run searchClass task`() {
        projectDir.resolve("build.gradle").writeText(
            """
            plugins {
                id 'java'
                id('io.github.redgreencoding.findclass')
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation 'org.apache.commons:commons-lang3:3.11'
            }
        """
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(
            "searchClass",
            "-Pfc.searchClass=**/StringUtils.*",
            "--warning-mode",
            "all"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("commons-lang3-3.11.jar"))
    }

    @Test
    fun `searchClass searches the whole classpath`() {
        projectDir.resolve("build.gradle").writeText(
            """
            plugins {
                id 'java'
                id('io.github.redgreencoding.findclass')
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation 'javax.xml.bind:jaxb-api:1.0.6'
                implementation 'jakarta.xml.bind:jakarta.xml.bind-api:2.3.2'
            }
        """
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(
            "searchClass",
            "-Pfc.searchClass=javax/xml/bind/JAXBException.*",
            "-Pfc.configurations=runtimeClasspath",
            "--warning-mode",
            "all"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("jaxb-api"))
        assertTrue(result.output.contains("jakarta.xml.bind-api"))
    }

    @Test
    fun `searchClass can filter out configurations`() {
        projectDir.resolve("build.gradle").writeText(
            """
            plugins {
                id 'java'
                id('io.github.redgreencoding.findclass')
            }
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                implementation 'org.apache.commons:commons-lang3:3.11'
            }
        """
        )

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(
            "searchClass",
            "-Pfc.searchClass=**/StringUtils.*",
            "-Pfc.configurations=runtimeClasspath",
            "--warning-mode",
            "all"
        )
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        assertTrue(result.output.contains("commons-lang3-3.11.jar"))
        assertFalse(result.output.contains("testCompileClasspath"))
        assertFalse(result.output.contains("testRuntimeClasspath"))
    }
}
