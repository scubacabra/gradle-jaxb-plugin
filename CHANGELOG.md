# Gradle JAXB Plugin Changelog

## 2.2.1
- Removed the synchronization from the XJC Ant Task call in favor of changing the default to the multi-process safe XJC2Task.

## 2.2.0
- Synchronized the XJC Ant Task call to allow for Gradle's new parallel project execution.

## 2.1.0
- Exposed the XJC Ant Tasks's classpath argument out to the plugin so that XJC Plugins can be used.

## 2.0.2
- Updated the examples build file to use the currently published version of the Rackspace flavored plugin.

## 2.0.1
- Updated the build to include the dependencies in the published POM.

## 2.0.0
- Added the Nebula GIT plugin to made the tag task work.

# Above this is from the open source Repose team.
# The remainder is from the original author.

## 1.3.6
- Fix to issue #27 - when there are binding files, will not parse xjc
  in a dependency order.  Will just use the folder and glob pattern
  '**/*.xsd'

## 1.3.5

- plugin package/group name changed from `org.gradle.jacobo.plugins`
to `com.github.jacobono`
- ant `xjc` task is passing in the episode file to generate through
  `arg value` instead of `arg line` to prevent errors when file paths
  have spaces in them.
- changing calls from member .size to .size()

## 1.3.4

- Plugin id changed from 'jaxb' to 'com.github.jacobono.jaxb'
- Included in gradle plugin repo

## 1.3.3

- Backwards compatibility with java 1.6
- now available via bintray's jcenter

## 1.3.2

- Adding additional args to plugin DSL

## 1.3.1

- full groovy doc and detailed documentation

## 1.3

- Major refactoring to use the gradle-xsd-wsdl-slurping common library
  I refactored the common logic and interfaces to. 
- Major refactoring of classes to improve readability
- Adding Service delegate Objects
- Using Guice for Dependency Injection
- Complete code makeover
- package renaming to one with base `org.gradle.jacobo.plugins`
- adding Test Fixtures to properly test the different edge cases of this plugin
- Tasks were renamed
- Added support for xsd projects to declare they depend on another xsd
  project and have that project's `xjc` task run prior. 
