# Gradle Findclass Plugin

which JAR is providing a given class?

Add the plugin to your Java/Kotlin project:

```
plugins {
    id 'java'
    id('com.github.abendt.findclass')
}
```

The plugin adds the task `findClass` that can tell you from which JAR a given Java class was loaded from:

`gw findClass -Pfc.findClass=org.apache.commons.lang3.StringUtils`

```
> Task :findClass
find class 'org.apache.commons.lang3.StringUtils'
+--- compileClasspath
|    `--- file:/Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
+--- runtimeClasspath
|    `--- file:/Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
+--- testCompileClasspath
|    `--- file:/Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar
`--- testRuntimeClasspath
     `--- file:/Users/abendt/.caches/modules-2/files-2.1/org.apache.commons/commons-lang3/3.11/68e9a6adf7cf8eb7e9d31bbc554c7c75eeaac568/commons-lang3-3.11.jar

```