package org.gradle.jacobo.plugins

import org.gradle.jacobo.plugins.extension.JaxbExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jacobo.plugins.JaxbPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.internal.project.DefaultProject
import org.gradle.jacobo.plugins.task.JaxbDependencyTree
import org.gradle.jacobo.plugins.task.JaxbXjc

class JaxbPluginSpec extends ProjectTaskSpecification {

  def rootFolder = getFileFromResourcePath("/test-project")
  def setupSpec() {
    def resourcesRoot = getFileFromResourcePath("/")
    def rootFolder = new File(resourcesRoot, "test-project")
    rootFolder.mkdir()
  }

  def cleanupSpec() {
    def rootFolder = getFileFromResourcePath("/test-project")
    rootFolder.delete()
  }

  def "applies Java plugin and adds extension object"() {
    expect:
    project.plugins.getPlugin("java") instanceof JavaPlugin
    with(project.jaxb) {
      it instanceof JaxbExtension
      xsdDir == "schema"
      episodesDir == "schema/episodes"
      bindingsDir == "schema/bindings"
      bindings == []
      with(xjc) {
      	destinationDir == "src/main/java"
      	producesDir == "src/main/java"
	extension == 'true'
      	removeOldOutput == 'yes'
	header == true
      }
    }
  }

  def "adds confiugration to this project"() {
    when:
    def jaxb = project.configurations.getByName(
      JaxbPlugin.JAXB_CONFIGURATION_NAME)

    then:
    jaxb.visible == true
    jaxb.transitive == true
    jaxb.description == "The JAXB XJC libraries to be used for this project."
  }

  def "contains 2 tasks, xjc task depends on tree graph task"() {
    when:
    def xjcTask = project.tasks['xjc']
    def dependencyTask = project.tasks['xsd-dependency-tree']

    then:
    [xjcTask, dependencyTask]*.group == ["parse", "parse"]
    dependencyTask instanceof JaxbDependencyTree
    xjcTask instanceof JaxbXjc
    xjcTask.taskDependencies.getDependencies(xjcTask)*.path as Set ==
      [':xsd-dependency-tree'] as Set
  }

  def "depends on other projects through jaxb configuration"() {
    given: "Two child projects from parent 'project'"
    def rootProject = ProjectBuilder.builder().withProjectDir(
      rootFolder).build()
    def commonProject = ProjectBuilder.builder().withName('common').withParent(
      rootProject).build()
    def lastProject = ProjectBuilder.builder().withName('last').withParent(
      rootProject).build()
    commonProject.apply(plugin: "com.github.jacobono.jaxb")
    lastProject.apply(plugin: "com.github.jacobono.jaxb")

    lastProject.dependencies {
      compile commonProject
      jaxb project(path: ':common', configuration: 'jaxb')
    }

    when:
    def buildTask = lastProject.tasks['buildNeeded']
    def xjcTask = lastProject.tasks['xjc']
    def treeTask = lastProject.tasks['xsd-dependency-tree']

    then:
    buildTask.taskDependencies.getDependencies(buildTask)*.path as Set ==
      [':last:build', ':common:buildNeeded'] as Set
    xjcTask.taskDependencies.getDependencies(xjcTask)*.path as Set ==
      [':last:xsd-dependency-tree'] as Set
    treeTask.taskDependencies.getDependencies(treeTask)*.path as Set ==
      [':common:xjc'] as Set
  }
}