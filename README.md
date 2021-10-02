# Gradle Findclass Plugin

which JAR is providing a given class?

Add the plugin to your Java/Kotlin project:

```
plugins {
    id 'java'
    id('io.github.redgreencoding.findclass')
}
```

## findClass

The plugin adds the task `findClass` that can tell you from which JAR a given Java class was loaded from. Use the property `-Pfc.findClass` to specifiy the full qualified classname. The plugin will try to load
the class using a Classloader, thus it will resolve the effective Class.

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

## searchClass

The plugin adds the task `searchClass` that can tell you from which JARs contain a given Java class. Use the property `-Pfc.searchClass` to specify a pattern (the pattern must also match the `.class` extension).
In contrast to the `findClass` task this will scan all available `*.jar` files. This means the same class might me found in different archives. Use the `findClass` task to determine which class will be effectively used.

`gw searchClass -Pfc.searchClass=**/StringUtils.*`

```
> Task :searchClass
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