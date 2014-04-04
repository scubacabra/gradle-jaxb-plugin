package org.gradle.jacobo.plugins

import org.gradle.jacobo.plugins.JaxbExtension
import org.gradle.api.plugins.JavaPlugin
import org.gradle.jacobo.plugins.JaxbPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.internal.project.DefaultProject
import org.gradle.jacobo.plugins.task.JaxbNamespaceTask
import org.gradle.jacobo.plugins.task.JaxbXJCTask

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
    when:
    def extension = project.extensions.jaxb

    then:
    project.plugins.getPlugin("java") instanceof JavaPlugin
    extension instanceof JaxbExtension
    extension.jaxbSchemaDirectory == "schema"
    extension.jaxbEpisodeDirectory == "schema/episodes"
    extension.jaxbBindingDirectory == "schema/bindings"
    extension.jaxbSchemaDestinationDirectory == "src/main/java"
    extension.extension == 'true'
    extension.removeOldOutput == 'yes'
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
    def dependencyTask = project.tasks['jaxb-generate-dependency-graph']

    then:
    [xjcTask, dependencyTask]*.group == ["parse", "parse"]
    dependencyTask instanceof JaxbNamespaceTask
    xjcTask instanceof JaxbXJCTask
    xjcTask.taskDependencies.getDependencies(xjcTask)*.path as Set ==
      [':jaxb-generate-dependency-graph'] as Set
  }

  def "depends on other projects through jaxb configuration"() {
    given: "Two child projects from parent 'project'"
    def rootProject = ProjectBuilder.builder().withProjectDir(
      rootFolder).build()
    def commonProject = ProjectBuilder.builder().withName('common').withParent(
      rootProject).build()
    def lastProject = ProjectBuilder.builder().withName('last').withParent(
      rootProject).build()
    commonProject.apply(plugin: "jaxb")
    lastProject.apply(plugin: "jaxb")

    lastProject.dependencies {
      compile commonProject
      jaxb project(path: ':common', configuration: 'jaxb')
    }

    when:
    def buildTask = lastProject.tasks['buildNeeded']
    def xjcTask = lastProject.tasks['xjc']
    def treeTask = lastProject.tasks['jaxb-generate-dependency-graph']

    then:
    buildTask.taskDependencies.getDependencies(buildTask)*.path as Set ==
      [':last:build', ':common:buildNeeded'] as Set
    xjcTask.taskDependencies.getDependencies(xjcTask)*.path as Set ==
      [':last:jaxb-generate-dependency-graph'] as Set
    treeTask.taskDependencies.getDependencies(treeTask)*.path as Set ==
      [':common:xjc'] as Set
  }
}