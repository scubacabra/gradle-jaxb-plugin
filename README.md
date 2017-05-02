gradle-jaxb-plugin
==================

:exclamation:IMPORTANT PLUGIN ID CHANGES:exclamation:

This plugin is an update to the original project this was forked from.
We acknowledge and are grateful to these developers for their
contributions to open source. You can find the source code of their 
original using the `forked from` link of this project. In compliance
with the license chosen by the original author, we are publishing this
modified version since they have not kept up with the maintenance needs. 

To prevent possible collisions and/or confusion if the original author
decides to accept our PR's or to simply begin anew, we have changed the
id and package names.

In compliance with the gradle plugin submission guidelines, the plugin's
id was changed from `com.github.jacobono.jaxb` to
`org.openrepose.gradle.plugins.jaxb`.  This affects how you apply the
plugin (`apply plugin: 'org.openrepose.gradle.plugins.jaxb'`)

Gradle plugin that defines some conventions for xsd projects and
provides some processing to ease some of the maintenance of these
projects by:

* Hooking in ant tasks to parse the xsd with the `xjc` task.
* Generates code from xsds per unique namespace.
* Generates an xsd dependency tree, to parse namespaces in their
  order of dependencies, from the base namespaces up.
* Generating an episode file for every unique namespace in a set
  of xsd files
* Defining a convention to place generated episode files
* Ability to define xsd projects to depend on one another, so
  that when parsing, what a project depends on is also parsed

Using The Plugin
================

See this plugin's page in the
[gradle plugins repo](https://plugins.gradle.org/plugin/org.openrepose.gradle.plugins.jaxb).

Setting Up The JAXB Configurations
==================================

You *need* the jaxb configuration to run the `xjc` task, but that is the
only task that requires an external dependency.
If an XJC plugin is used, then simply add it to the dependencies to
have it included.

Any version of jaxb that you care to use will work.

```groovy
    dependencies { 
      jaxb 'com.sun.xml.bind:jaxb-xjc:2.2.7-b41'
      jaxb 'com.sun.xml.bind:jaxb-impl:2.2.7-b41'
      jaxb 'javax.xml.bind:jaxb-api:2.2.7'
      xjc  'com.example:xjc-plugin:0.0.0'
    }
```

Plugin Tasks
============

There are only two tasks.

* `xjc`
	- runs `xjc` ant task on each `.xsd` in the dependency tree.
	- needs to be run **manually**.
* `xsd-dependency-tree`
	- Builds a dependency tree from all `.xsd` files configured to be
      parsed.
	- Finds each unique namespace and groups files containing that
      namespace
	- Analyzes xsd dependencies and places them in the correct place
      in the dependency tree so that the namespaces can be parsed in
      order using their generated episode files to bind, allowing
      other projects to use the episode files generated from the
      namespace in the tree.
	  - This keeps all namespaces decoupled and prevents a big
        episode blob containing everything that was parsed.

`xjc` depends on `xsd-dependency-tree` so you don't need to run the
tree task at all.

Plugin Conventions
==================

There are two conventions that can be overridden and one is nested in
the other.

The `jaxb` convention defines the conventions for the whole plugin,
and the `xjc` convention defines the conventions for the `xjc` ant
task.

You can change these defaults with a closure in your build
script.

```groovy
    jaxb {
	  ...
      xjc {
	    ...
	  }
    }
```

## JAXB Plugin Convention ##

There are 4 overrideable defaults for this JAXB Plugin.
These defaults are changed via the `jaxb` closure.

* `xsdDir`
	* Defined **by each** project to tell the plugin where to find the
      `.xsd` files to parse
* `xsdIncludes`
  * the schemas to compile
  * file name List of strings found in `xsdDir`
  * The default glob pattern is `**/*.xsd`
* `episodesDir`
	* i.e. _"build/generated-resources/episodes"_, _"episodes"_,
	    _"schema/episodes"_, _"xsd/episodes"_, _"XMLSchema/episodes"_
	* **All** generated episode files go directly under here, no
	    subfolders.
* `bindingsDir`
	* i.e. "src/main/resources/schema", "bindings", "schema/bindings",
	    "xsd/bindings", "XMLSchema/bindings"
    * User defined binding files to pass in to the `xjc` task
	* **All** files are directly under this folder, _no subfolders_.
* `bindings`
  * customization files to bind with
  * file name List of strings found in `bindingsDir`
  * The default glob pattern is `**/*.xjb`

## XJC Convention ##

These defaults are changed via the nested `xjc` closure.
Several sensible defaults are defined to be passed into the
`wsimport` task:

| parameter				 | Description									    | default		                | type	  |
| :---					 | :---:										    | :---:			                | ---:	  |
|`destinationDir` _(R)_	 | generated code will be written to this directory | `${project.buildDir}/generated-sources/xjc` | `String`  |
|`extension` _(O)_		 | Run XJC compiler in extension mode			    | `true`		                | `boolean` |
|`header` _(O)_			 | generates a header in each generated file	    | `true`		                | `boolean` |
|`producesDir` _(O)_ | aids with XJC up-to-date check				    | `${project.buildDir}/generated-sources/xjc` | `String`  |
|`generatePackage` _(O)_ | specify a package to generate to				    | **none**		                | `String`  |
|`args` _(O)_ | List of strings for extra arguments to pass that aren't listed | **none**                   | `List<String>` |
|`removeOldOutput` _(O)_ | Only used with nested `<produces>` elements, when _'yes'_ all files are deleted before XJC is run | _'yes'_ | `String` |
|`taskClassname` _(O)_ | Enables a custom task classname if using something other than jaxb | `com.sun.tools.xjc.XJCTask` | `String` |
|`accessExternalSchema` _(O)_ | Enables setting the new `javax.xml.accessExternalSchema` system property that causes the plugin to not work as expected under JSE8. | **Implementation Specific** | `String` | 

* (O) - optional argument
* (R) - required argument

For more in depth description please see
https://jaxb.java.net/2.2.7/docs/ch04.html#tools-xjc-ant-task -- or
substitute the version you are using.

### destinationDir ###

`destinationDir` is relative to `project.rootDir`.  It is defaulted to
`${project.buildDir}/generated-sources/xjc`, but can be set to anywhere.

### producesDir ###

`producesDir` is not currently used in the plugin.  But it was meant
to be in there so that if no xsd changes have happened, then no
code generation would take place.  *Hasn't worked yet.*

### taskClassname ###

`taskClassname` is the class to be used to run the xjc task.  Useful if
JAXB2 is desired to be used.

### Extra Arguments ###
`args` passes arbitrary arguments to the `xjc` ant task. This is useful
when activating JAXB2 plugins.

# Examples #

## Default Example using JAXB ##

If the default conventions aren't changed, the only thing to configure
_(per project)_ is the `xsdDir`, and jaxb dependencies as described above.

```groovy
jaxb {
  xsdDir = "${project.projectDir}/schema/folder1"
}
```

## Default Example using JAXB2 ##

Customized to use the same `xjc` task that
[xjc-mojo](http://mojo.codehaus.org/jaxb2-maven-plugin/xjc-mojo.html)
uses.

```groovy
dependencies {
  jaxb "org.jvnet.jaxb2_commons:jaxb2-basics-ant:0.6.5"
  jaxb "org.jvnet.jaxb2_commons:jaxb2-basics:0.6.4"
  jaxb "org.jvnet.jaxb2_commons:jaxb2-basics-annotate:0.6.4"
}

jaxb {
  xsdDir = "${project.projectDir}/some/folder"
  xjc {
     taskClassname      = "org.jvnet.jaxb2_commons.xjc.XJC2Task"
	 generatePackage    = "com.company.example"
     args               = ["-Xinheritance", "-Xannotate"]
  }
}
```

Defining The Plugin For All Projects
====================================

Create a convention for xsd projects to have a suffix of `-schema`, then
it is easy to write:

```groovy
subprojects { project ->
  if(project.name.endsWith("-schema")) { 
    apply plugin: 'org.openrepose.gradle.plugins.jaxb'

    dependencies { 
      jaxb 'com.sun.xml.bind:jaxb-xjc:2.2.7-b41'
      jaxb 'com.sun.xml.bind:jaxb-impl:2.2.7-b41'
      jaxb 'javax.xml.bind:jaxb-api:2.2.7'
    }
  }
}
```

applying the plugin to all schema projects.

Another way to do this is by adding a boolean property to the
`gradle.properties` file in the sub-projects. You can then use it this way:
  
```groovy
subprojects { project ->
  if(Boolean.valueOf(project.getProperties().getOrDefault('doJAXB', 'false'))) { 
    apply plugin: 'com.github.jacobono.jaxb'

    dependencies { 
      jaxb 'com.sun.xml.bind:jaxb-xjc:2.2.7-b41'
      jaxb 'com.sun.xml.bind:jaxb-impl:2.2.7-b41'
      jaxb 'javax.xml.bind:jaxb-api:2.2.7'
    }
  }
}
```

Other Features
==============

## Depend On Another Project

This lets gradle know that the xjc task of a project is dependent on
the xjc task of another project.  This can be achieved with:

```groovy
dependencies {
  jaxb project(path: ':common', configuration: 'jaxb')
}
```

This expresses that xsd's definitely depend on other xsd's outside of
their parent folder `xsdDir`.

This will run the xjc task on `common` before running the xjc task of
of the project this is defined in.

Examples
========

You can find some small example projects using this plugin in the
[examples folder](examples).

For a basic example of using this plugin with multiple sub-projects that
have interactions, please see this [test project](https://github.com/wdschei/gradle-jaxb-plugin-test).

For a real world example of this plugin, please visit the main
[Repose project](https://github.com/rackerlabs/repose).

Improvements
============

If you have an idea that would make something a little easier, we'd love
to hear about it. If you think you can make this plugin better, then
simply fork it like we did and submit a pull request.
