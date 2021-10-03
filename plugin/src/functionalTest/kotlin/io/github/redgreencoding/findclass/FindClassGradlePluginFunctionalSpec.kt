package io.github.redgreencoding.findclass

import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.internal.PluginUnderTestMetadataReading
import java.io.File

class FindClassGradlePluginFunctionalSpec : StringSpec() {

    private val projectDir = File("build/functionalTest")

    private val gradleArgs = mutableListOf<String>()

    override fun beforeSpec(spec: Spec) {
        if (projectDir.exists()) {
            projectDir.deleteRecursively()
        }

        projectDir.mkdirs()
        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("gradle.properties").writeText("org.gradle.unsafe.configuration-cache-problems=warn")
    }

    override fun beforeTest(testCase: TestCase) {
        gradleArgs.clear()
    }

    init {
        "can run resolveClass task" {
            givenGradleFile(
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

            givenArgs(
                "resolveClass",
                "--classname=org.apache.commons.lang3.StringUtils"
            )

            gradleShouldSucceed {
                output shouldContain "commons-lang3-3.11.jar"
            }
        }

        "resolveClass should fail without option" {
            givenGradleFile(
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

            givenArgs("resolveClass")

            gradleShouldFail()
        }

        "resolveClass can print help" {
            givenGradleFile(
                """
            plugins {
                id 'java'
                id('io.github.redgreencoding.findclass')
            }
            
            repositories {
                mavenCentral()
            }
        """
            )

            givenArgs(
                "help",
                "--task",
                "resolveClass"
            )

            gradleShouldSucceed { }
        }

        "can use as init script" {
            val gradleHomeDir = File("build/gradleHome")
            gradleHomeDir.mkdirs()

            val initDir = File(gradleHomeDir, "init.d")
            initDir.mkdirs()

            initDir.resolve("findClass.gradle").writeText(
                """initscript {
                    dependencies {
                        classpath files(${
                PluginUnderTestMetadataReading.readImplementationClasspath()
                    .joinToString(separator = ",") { "'$it'" }
                })
                    }
                }
                rootProject {
                    apply plugin: io.github.redgreencoding.findclass.FindClassGradlePlugin
                }
                """.trimIndent()
            )

            givenGradleFile(
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

            givenArgs(
                "-Dgradle.user.home=${gradleHomeDir.absolutePath}",
                "resolveClass",
                "--classname=org.apache.commons.lang3.StringUtils"
            )

            gradleShouldSucceed {
                output.shouldContain("commons-lang3-3.11.jar")
            }
        }

        "resolveClass can filter out configurations" {
            givenGradleFile(
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

            givenArgs(
                "resolveClass",
                "--classname=org.apache.commons.lang3.StringUtils",
                "--configurations=runtimeClasspath"
            )

            gradleShouldSucceed {
                output.shouldContain("commons-lang3-3.11.jar")
                output.shouldNotContain("testCompileClasspath")
                output.shouldNotContain("testRuntimeClasspath")
            }
        }

        "can run scanConfigurations task" {
            givenGradleFile(
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

            givenArgs(
                "scanConfigurations",
                "--pattern=**/StringUtils.*"
            )

            gradleShouldSucceed {
                output.shouldContain("commons-lang3-3.11.jar")
            }
        }

        "scanConfigurations searches the whole classpath" {
            givenGradleFile(
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

            givenArgs(
                "scanConfigurations",
                "--pattern=javax/xml/bind/JAXBException.*"
            )

            gradleShouldSucceed {
                output.shouldContain("jaxb-api")
                output.shouldContain("jakarta.xml.bind-api")
            }
        }

        "scanConfigurations can filter out configurations" {
            givenGradleFile(
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

            givenArgs(
                "scanConfigurations",
                "--pattern=**/StringUtils.*",
                "--configurations=runtimeClasspath"
            )

            gradleShouldSucceed {
                output.shouldContain("commons-lang3-3.11.jar")
                output.shouldNotContain("testCompileClasspath")
                output.shouldNotContain("testRuntimeClasspath")
            }
        }
    }

    private fun givenGradleFile(file: String) {
        projectDir.resolve("build.gradle").writeText(
            file
        )
    }

    private fun givenArgs(vararg args: String) {
        gradleArgs.addAll(args)
    }

    private fun gradleShouldSucceed(check: BuildResult.() -> Unit = {}) {
        val result = newGradleRunner().build()

        check(result)
    }

    private fun gradleShouldFail(check: BuildResult.() -> Unit = {}) {
        val result = newGradleRunner().buildAndFail()

        check(result)
    }

    private fun newGradleRunner(): GradleRunner {
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments(*gradleArgs.toTypedArray(), "--warning-mode", "all")
        runner.withProjectDir(projectDir)
        return runner
    }
}
