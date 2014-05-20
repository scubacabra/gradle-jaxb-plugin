gradle-jaxb-plugin
==================

[![Build Status](https://drone.io/github.com/jacobono/gradle-jaxb-plugin/status.png)
](https://drone.io/github.com/jacobono/gradle-jaxb-plugin/latest)

[ ![Download](https://api.bintray.com/packages/djmijares/gradle-plugins/gradle-jaxb-plugin/images/download.png)
](https://bintray.com/djmijares/gradle-plugins/gradle-jaxb-plugin/_latestVersion)

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

```groovy
buildscript {
  repositories {
    maven { 
      url 'http://dl.bintray.com/content/djmijares/gradle-plugins' 
    }
    mavenCentral()
  }

  dependencies {
    classpath 'com.jacobo.gradle.plugins:gradle-jaxb-plugin:1.3.1'
  }
}

apply plugin: 'jaxb'
```

Setting Up The JAXB Configurations
==================================

You *need* the jaxb configuration to run the `xjc` task, but that is the
only task that has an external dependency.

Any version of jaxb that you care to use will work.  I try to stay with the latest releases.

```groovy
    dependencies { 
      jaxb 'com.sun.xml.bind:jaxb-xjc:2.2.7-b41'
      jaxb 'com.sun.xml.bind:jaxb-impl:2.2.7-b41'
      jaxb 'javax.xml.bind:jaxb-api:2.2.7'
    }
```

Plugin Tasks
============

There are only two tasks.

* `xjc`
	- runs xjc ant task on each of the xsds in the dependency tree.
	- needs to be run manually.
* `xsd-dependency-tree`
	- Builds a dependency tree from all the xsd files configured to be
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

There are 4 overridable defaults for this JAXB Plugin.
These defaults are changed via the `jaxb` closure.

* xsdDir
  * **ALWAYS** relative to `project.rootDir
	* Defined **by each** project to tell the plugin where to find the
      xsds to parse
* episodesDir
  * **ALWAYS** relative to `project.rootDir
	* i.e. "episodes", "schema/episodes", "xsd/episodes",
      "XMLSchema/episodes"
	* All generated episode files go directly under here, no subfolders.
* bindingsDir
  * **ALWAYS** relative to `project.rootDir
	* i.e. "bindings", "schema/bindings", "xsd/bindings",
      "XMLSchema/bindings"
    * User defined binding files to pass in to the `xjc` task
	* All files are directly under this folder, no subfolders.

## XJC Convention ##

These defaults are changed via the nested `xjc` closure.
Several boolean sensible defaults are defined to be passed into the
wsimport task:

* extension
* removeOldOutput
* header

And a few other String defaults

* destinationDir
* producesDir

`destinationDir` is relative to `project.projectDir`.  It is
defaulted to `src/main/java`, but can be set to anywhere in the
`project.projectDir`.

`producesDir` is not currently used in the plugin.  But it was meant
to be in there so that if no xsd changes have happened, then no
code generation would take place.  Hasn't worked yet.

## Default Conventions

These are the current default conventions:

```groovy
jaxb {
  xsdDir			= "schema"
  episodesDir		= "schema/episodes"
  bindingsDir		= "schema/bindings"
  bindings			= []
  xjc {
     destinationDir		= "src/main/java"
	 producesDir		= "src/main/java"
	 extension			= 'true'
	 removeOldOutput	= 'yes'
	 header				= true
  }
}
```

If the default conventions aren't changed, the only thing to configure
_(per project)_ is the `xsdDir`.

```groovy
jaxb {
  xsdDir = "schema/folder1"
}
```

Defining The Plugin For All Projects
====================================

I like to create a convention for xsd projects to have a suffix of
`-schema`.  I find it easy to then write:

```groovy
subproject { project ->
  if(project.name.endsWith("-schema")) { 
    apply plugin: 'jaxb'

    dependencies { 
      jaxb 'com.sun.xml.bind:jaxb-xjc:2.2.7-b41'
      jaxb 'com.sun.xml.bind:jaxb-impl:2.2.7-b41'
      jaxb 'javax.xml.bind:jaxb-api:2.2.7'
    }
  }
}
```

applying the plugin to all schema projects.

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

I like how this expresses that xsd's definitely depend on other xsd's
outside of their parent folder `xsdDir`.

This will run the xjc task on `common` before running the xjc task of
of the project this is defined in.

Examples
========

You can find some examples in the [examples folder](examples)

Improvements
============

If you think this plugin could be better, please fork it! If you have an idea
that would make something a little easier, I'd love to hear about it.
