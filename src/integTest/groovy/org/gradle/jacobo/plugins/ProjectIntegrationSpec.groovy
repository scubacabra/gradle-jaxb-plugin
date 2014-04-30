package org.gradle.jacobo.plugins

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.jacobo.plugins.JaxbPlugin
import org.gradle.jacobo.plugins.ProjectTaskSpecification
import org.gradle.jacobo.plugins.task.JaxbDependencyTree
import org.gradle.jacobo.plugins.task.JaxbXjc

class ProjectIntegrationSpec extends ProjectTaskSpecification {
  // Task names
  def dependenyTreeTaskName = JaxbPlugin.JAXB_XSD_DEPENDENCY_TREE_TASK
  def xjcTaskName = JaxbPlugin.JAXB_XJC_TASK

  // tasks
  def dependencyTreeTask
  def xjcTask

  // root project to add sub project too, like a real situation would consist of
  def rootProject

  def setRootProject(def rootDir) {
    rootProject = ProjectBuilder.builder().withProjectDir(rootDir).build()
  }
  
  def setSubProject(def rootProject, def projectName, def plugin) {
    project = ProjectBuilder.builder().withName(projectName).withParent(rootProject).build()
    project.apply(plugin: plugin)
  }

  def setupProjectTasks() {
    dependencyTreeTask = project.tasks[dependenyTreeTaskName] as JaxbDependencyTree
    xjcTask = project.tasks[xjcTaskName] as JaxbXjc
  }
}