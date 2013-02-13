gradle-jaxb-plugin
================================

gradle-jaxb-plugin is a plugin for aiding with separate compilation of all xsd's in a particular folder. Then to generate a namespace dependency graph to parse those namespace files first, generate episode files and use them in binding so everything is done clean and fast for you without all the fuss

using the plugin
----------
```groovy
buildscript {
  repositories { 
    ivy { 
      url 'http://dl.bintray.com/content/djmijares/gradle-plugins' 
    }
  }

  dependencies {
    classpath 'com.jacobo.gradle.plugins:gradle-jaxb-plugin:1.0'
  }
}

apply plugin: 'jaxb'
```
# Possible configurable Properties #
```groovy
String jaxbSchemaDirectory
String jaxbEpisodeDirectory
String jaxbBindingDirectory
String jaxbSchemaDestinationDirectory
String jaxbProducesDirectory

String xsdDirectoryForGraph

String extension
String removeOldOutput
boolean header

List bindingIncludes = []
```

## Default Properties ##
```groovy
jaxbSchemaDirectory = "${project.rootDir}/schema"
jaxbEpisodeDirectory = "${project.rootDir}/schema/episodes" 
jaxbBindingDirectory = "${project.rootDir}/schema/bindings"
jaxbSchemaDestinationDirectory = "src/main/java"
extension = 'true'
removeOldOutput = 'yes'
```

If you stick with the default settings, the only thing you have to configure is the `jaxbSchemaDirectory` for every sub project

```groovy
jaxb {
  jaxbSchemaDirectory = "path/to/schema/folder"
}
```

If you need to configure the default folders because you already have something going, you can do it in the root Directory build.gradle file

```groovy
jaxb {
   jaxbSchemaDirectory = "${project.rootDir}/XMLSchema"
   jaxbEpisodeDirectory = "${project.rootDir}/XMLSchema/Episodes" 
   jaxbBindingDirectory = "${project.rootDir}/XMLSchema/Bindings"
}
```

# Configurations for the parsing #
The task `xjc` needs the jaxb tools on its classpath, whichever version should work fine. 

```groovy
subproject { project ->
  if(project.name.endsWith("-schema")) { 
    apply plugin: 'jaxb-namespace'
    apply plugin: 'java'

    dependencies { 
      jaxb 'com.sun.xml.bind:jaxb-xjc:2.2.7-b41'
      jaxb 'com.sun.xml.bind:jaxb-impl:2.2.7-b41'
      jaxb 'javax.xml.bind:jaxb-api:2.2.7'
    }
  }
}
```

This is what I tend to do for my multi project builds, I like filtering on the project name

# Tasks #
There are only two tasks.

1. `jaxb-generate-dependency-graph`

2. `xjc`


`xjc` depends on `jaxb-generate-dependency-graph` so you don't need to run it at all. 

## jaxb-generate-dependency-graph ##

This task starts it's processing in the `jaxbSchemaDirectory` folder.  It finds all the xsd's in this folder and finds all the unique namespaces that are defined by the xsd `targetNamespace` attribute found at the root (`schema`) element. 

It then generates a dependency graph starting with the base schemas that don't import any other schemas, and find the next group of namespaces that depend on the base but on no more than the base, and so on etc. until the graph is full.  

Then the each namespace "level" is looped over and parsed with the `xjc` task.  Each namespace generates it's own episode file, and when a schema imports a certain namespace, it binds to the episode file.  

Each episode is named based after it's namespace.  it is really just the full namespace minus some illegal characters. 

This allows you to:
* group schema documents based on what the schema is modeling, and you can have all the unique namespaces you want.  
* Minimizes duplicate schema generation.
  - i.e if folder `schema/Kitchen/` has imports from `schema/LivingRoom` you can parse `schema/LivingRoom` in a subproject, and then when you parse `schema/Kitchen` it will automatically resolve "external namespace" i.e. namespace not present in the folder `jaxbSchemaDirectory` and will parse for dependencies and bind with the appropriate episode files.  Not more regeneration, which always irked me a little bit too much.  

## xjc ##
This is just the ant xjc task.  

The `dependency` task gives it a list of namespaces to parse and it goes through one by one until it has parsed all the namespaces and all the files in those namespaces
  
