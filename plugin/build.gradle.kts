plugins {
    `java-gradle-plugin`
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.14.0"

    id("org.jetbrains.kotlin.jvm") version "1.4.31"
}

group = "com.github.abendt"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.barfuin.texttree:text-tree:2.1.1")

    testImplementation("org.jetbrains.kotlin:kotlin-test")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

gradlePlugin {
    val greeting by plugins.creating {
        id = "com.github.abendt.findclass"
        implementationClass = "com.github.abendt.findclass.GradleFindclassPlugin"
    }
}

pluginBundle {
    website = "https://github.com/abendt/gradle-findclass-plugin"
    vcsUrl = "https://github.com/abendt/gradle-findclass-plugin.git"
    tags = listOf("gradle", "findclass", "dependencies")
}

publishing {
    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../build/local-plugin-repository")
        }
    }
}

val functionalTestSourceSet = sourceSets.create("functionalTest") {
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
