# Gradle Findclass Plugin

which JAR is providing a given class?

## Installation

### project

Add the plugin to your Java/Kotlin project:

```groovy
plugins {
    id 'java'
    id('io.github.redgreencoding.findclass')
}
```

### per user (init script)

```groovy
initscript {
    repositories {
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("io.github.redgreencoding:findclass-gradle-plugin:0.0.1")
    }
}

rootProject {
    apply plugin: io.github.redgreencoding.findclass.FindClassGradlePlugin
}
```

## Usage

### resolveClass

The plugin adds the task `resolveClass` that can tell you from which JAR a given Java class was loaded from. Use the option `--classname=<fqn>` to specify the full qualified classname. The plugin will try to load
the class using a Classloader, thus it will resolve the effective Class.

`gradle resolveClass --classname=org.apache.commons.lang3.StringUtils`

```
> Task :resolveClass
resolve class 'org.apache.commons.lang3.StringUtils'
+--- compileClasspath
|    `--- file:/Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
+--- runtimeClasspath
|    `--- file:/Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
+--- testCompileClasspath
|    `--- file:/Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
`--- testRuntimeClasspath
     `--- file:/Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
```

By default `resolveClass` will search all Gradle configurations. You can use the option `--configurations=...` to limit the configurations that should be searched. Example usage:
`--configurations=compileClasspath,runtimeClasspath`.

### scanConfigurations

The plugin adds the task `scanConfigurations` that can tell you from which JARs contain a given Java class. Use the property `--pattern` to specify a pattern.
Check the [PatternFilterable API docs](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/util/PatternFilterable.html) for the syntax. Note: the pattern must also match the `.class` extension!
In contrast to the [resolveClass](#resolveclass) task `scanConfigurations` will scan all available `*.jar` archives. This means the same class might be found in separate locations. 
Use the `resolveClass` task to determine which class will be effectively used.

`gradle scanConfigurations -Ppattern=**/StringUtils.*`

```
> Task :scanConfigurations
search class with pattern '**/StringUtils.*'
+--- compileClasspath
|    `--- /Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
+--- runtimeClasspath
|    `--- /Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
+--- testCompileClasspath
|    `--- /Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
`--- testRuntimeClasspath
     `--- /Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
```

By default `scanConfigurations` will search all Gradle configurations. You can use the option `--configurations=...` to limit the configurations that should be searched. Example usage:
`--configurations=compileClasspath,runtimeClasspath`.
