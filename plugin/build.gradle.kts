plugins {
    `java-gradle-plugin`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "1.2.1"

    // this should be set to 1.5.31 as this is the bundled version of gradle 7.3
    id("org.jetbrains.kotlin.jvm") version "1.9.23"

    id("com.diffplug.spotless") version "6.25.0"
}

group = "io.github.redgreencoding"
version = project.findProperty("projectVersion") ?: "0.0.3-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(gradleApi())

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.barfuin.texttree:text-tree:2.1.2")

    val koTestVersion = "5.8.1"
    testImplementation("io.kotest:kotest-runner-junit5:$koTestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$koTestVersion")
}

gradlePlugin {
    website.set("https://github.com/red-green-coding/findclass-gradle-plugin")
    vcsUrl.set("https://github.com/red-green-coding/findclass-gradle-plugin.git")

    plugins {
        create("findClass") {
            id = "io.github.redgreencoding.findclass"
            displayName = "FindClass Gradle Plugin"
            description = "A plugin that helps you find out from which .jar archive a class is loaded from"
            implementationClass = "io.github.redgreencoding.findclass.FindClassGradlePlugin"
            tags.set(listOf("findclass", "searchclass", "dependencies", "analyze", "classpath"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            artifactId = "findclass-gradle-plugin"
        }
    }

    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../build/local-plugin-repository")
        }
    }
}

val functionalTestSourceSet =
    sourceSets.create("functionalTest") {
    }

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

tasks.check {
    dependsOn(functionalTest)
}

tasks.withType(Test::class.java) {
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true

        incremental = true
        freeCompilerArgs =
            listOf(
                "-Xjsr305=strict",
            )
    }
}

tasks {
    validatePlugins {
        enableStricterValidation.set(true)
        failOnWarning.set(true)
    }

    jar {
        from(sourceSets.main.map { it.allSource })
        manifest.attributes.apply {
            put("Implementation-Title", "Gradle Kotlin DSL (${project.name})")
            put("Implementation-Version", archiveVersion.get())
        }
    }
}

spotless {
    ratchetFrom("origin/main")

    kotlin {
        ktlint()
    }

    kotlinGradle {
        ktlint()
    }
}
