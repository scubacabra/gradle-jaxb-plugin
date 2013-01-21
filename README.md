gradle-jaxb-namespace-dependency
================================

gradle-jaxb-namespace-dependency is a plugin for aiding with separate compilation of all xsd's in a particular folder. Then to generate a namespace dependency graph to parse those namespace files first, generate episode files and use them in binding so everything is done clean and fast for you without all the fuss

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

If you stick with the default, the only thing you have to configure is the `jaxbSchemaDirectory` for every sub project

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
The task xjc needs the jaxb tools on its classpath, whichever version should work fine. 

```groovy
subproject { project ->
  if(project.name.endsWith("-schema")) { 
    apply plugin: 'jaxb-namespace'
    apply plugin: 'java'

    dependencies { 
      jaxb 'com.sun.xml.bind:jaxb-xjc:2.2.7-b41' //jaxws 2.2.6 uses jaxb 2.2.5, but can't dL 2.2.5 from maven the pom is off TODO
      jaxb 'com.sun.xml.bind:jaxb-impl:2.2.7-b41'
      jaxb 'javax.xml.bind:jaxb-api:2.2.7'
    }
  }
}
```

This is what I tend to do for my multi project builds, I like filtering on the project name

